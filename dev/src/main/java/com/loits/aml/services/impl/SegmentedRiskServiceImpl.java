package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.commons.CalcStatusCodes;
import com.loits.aml.commons.RiskCalcParams;
import com.loits.aml.config.Translator;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.AmlRisk;
import com.loits.aml.domain.CalcTasks;
import com.loits.aml.dto.Customer;
import com.loits.aml.kafka.services.KafkaProducer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.AmlRiskRepository;
import com.loits.aml.repo.CalcStatusRepository;
import com.loits.aml.repo.GeoLocationRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class SegmentedRiskServiceImpl implements SegmentedRiskService {

  Logger logger = LogManager.getLogger(SegmentedRiskServiceImpl.class);

  @Autowired
  Environment env;

  @Autowired
  KafkaProducer kafkaProducer;

  @Autowired
  AMLRiskService amlRiskService;

  @Autowired
  CalcStatusService calcStatusService;

  @Autowired
  KieService kieService;

  @Autowired
  ModuleRepository moduleRepository;

  @Autowired
  GeoLocationRepository geoLocationRepository;

  @Autowired
  CalcStatusRepository calcStatusRepository;

  @Autowired
  HTTPService httpService;

  @Autowired
  AmlRiskRepository amlRiskRepository;

  @Value("${loits.tp.size}")
  int THREAD_POOL_SIZE;

  @Value("${loits.tp.queue.size}")
  int THREAD_POOL_QUEUE_SIZE;

  @Value("${aml.risk-calculation.expiry.hours}")
  int RISK_EXPIRY_PERID; // IN HOURS

  @Value("${aml.risk-calculation.segment-size}")
  int SEGMENT_SIZE;

  @Value("${global.date.format}")
  private String dateFormat;

  SimpleDateFormat sdf;

  @PostConstruct
  public void init() {
    this.sdf = new SimpleDateFormat(dateFormat);
  }

  @Override
  public CompletableFuture<?> calculateCustomerSegmentRisk(RiskCalcParams riskCalcParams,
                                                           Long calId, String user, String tenent,
                                                           int page,
                                                           int size) {
    return CompletableFuture.runAsync(() -> {

      try {
        TenantHolder.setTenantId(tenent);
        calculate(riskCalcParams, calId, user, tenent, page, size);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        // clear tenant
        TenantHolder.clear();
      }

    });
  }


  @Override
  public Object calculateRiskForBatch(String user, String tenent, RiskCalcParams riskCalcParams) throws FXDefaultException {
    if (riskCalcParams.getPageLimit().intValue() < 0 || riskCalcParams.getRecordLimit().intValue() < 0) {
      throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("INVALID_INDEX"),
              new Date(), HttpStatus.BAD_REQUEST, false);
    }

    int size = 0, page = 0;

    if (riskCalcParams.getPageLimit().intValue() == -1) {
      size = Integer.MAX_VALUE;
    } else {
      size = riskCalcParams.getRecordLimit().intValue();
      page = riskCalcParams.getPageLimit().intValue();
    }

    logger.debug("Batchwise risk calculation process started with size " + size + " and page " +
            "number " + page + ".Tenent " + tenent);

    calculate(riskCalcParams, -1l, user, tenent, page, size);
    return true;
  }

  private void calculate(RiskCalcParams riskCalcParams, Long calId, String user, String tenent,
                         int page,
                         int size) {
    HashMap<String, Object> meta = new HashMap<>();
    List<AmlRisk> customerSetmentRisks = new ArrayList<>();

    meta.put("page", page);
    meta.put("size", size);

    // Log sync status for this segment - init status
    CalcTasks thisTask = this.calcStatusService.saveCalcTask(new CalcTasks(), calId,
            String.valueOf(Thread.currentThread().getId()),
            CalcStatusCodes.CALC_INITIATED, meta);

    try {
      logger.debug(String.format("Starting to calculate risk for tenent : %s , page: %s , size:" +
              " %s", tenent, page, size));

      List<Customer> customerList = null;
      //Customer customer = null;
      ObjectMapper objectMapper = new ObjectMapper();
      int errorCount = 0, successCount = 0;

      //Request parameters to Customer Service
      String customerServiceUrl = String.format(env.getProperty("aml.api.customer"), tenent);
      HashMap<String, String> parameters = new HashMap<>();
      parameters.put("page", String.valueOf(page));
      parameters.put("sort", "id,asc");
      parameters.put("size", String.valueOf(size));

      if (RISK_EXPIRY_PERID != 0) {
        logger.debug("Risk calculation expiry date set");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, -(RISK_EXPIRY_PERID));
        parameters.put("lastRiskCalculatedDateBefore", String.valueOf(cal.getTimeInMillis()));
      }

      try {
        logger.debug("Sending request to Customer API to get Customer");
        customerList = httpService.getDataFromPage("Customer", customerServiceUrl, parameters,
                new TypeReference<List<Customer>>() {
                });
        logger.debug("Customers successfully retrieved");
      } catch (Exception e) {
        logger.debug("Customer retrieval failed with " + e.getMessage());
        e.printStackTrace();
        // update calc status
        this.calcStatusService.saveCalcTask(thisTask, calId,
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_ERROR, meta);

        this.calcStatusService.saveCalcLog(thisTask, "Customer risk calculation unknown error",
                e.getMessage(), "CustomerId", "", "Customer", e);

        return;
      }

      if (customerList != null && !customerList.isEmpty()) {
        meta.put("fetched", customerList.size());

        // update calc status
        this.calcStatusService.saveCalcTask(thisTask, calId,
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_UPDATED, meta);

        for (Customer customer : customerList) {

          try {
            AmlRisk rsk = amlRiskService.runRiskCronJob(riskCalcParams, user, tenent, customer);
            if (rsk != null) {
              customerSetmentRisks.add(rsk);
            }
            logger.debug("Risk calculated for customer with id " + customer.getId());
            //customer.setRiskCalculationStatus(customer.getVersion());
            successCount += 1;
          } catch (Exception e) {
            e.printStackTrace();
            this.calcStatusService.saveCalcLog(thisTask, "Customer risk calculation failed",
                    e.getMessage(), "CustomerId", String.valueOf(customer.getId()), "Customer",
                    e);
            logger.error("Risk not calculated for customer id " + customer.getId());
            errorCount += 1;
          }
        }
      } else {
        logger.warn("Did not load any customers for risk calculation");
        meta.put("fetched", 0);
      }

      // save customer risk status
      if (!customerSetmentRisks.isEmpty()) {
        Iterable it = amlRiskRepository.saveAll(customerSetmentRisks);
        logger.debug("AmlRisk record saved to database successfully");

        customerSetmentRisks.forEach(cr -> {
          kafkaProducer.publishToTopic("aml-risk-create", cr);
          amlRiskService.saveRiskCalculationTime(cr.getCustomer(), cr.getRiskCalcAttemptDate(),
                  tenent);
        });
      }

      meta.put("processed", successCount);
      meta.put("updated", customerSetmentRisks.size());
      meta.put("errorCount", errorCount);

      // update calc status
      this.calcStatusService.saveCalcTask(thisTask, calId,
              String.valueOf(Thread.currentThread().getId()),
              CalcStatusCodes.CALC_COMPLETED, meta);

      logger.info(String.format("Risk calculation for page : %s completed", page));

    } catch (
            Exception e) {
      logger.error("Risk Calculation for segment - process error");

      // update calc status
      this.calcStatusService.saveCalcTask(thisTask, calId,
              String.valueOf(Thread.currentThread().getId()),
              CalcStatusCodes.CALC_ERROR, meta);

      this.calcStatusService.saveCalcLog(thisTask, "Customer risk calculation unknown error",
              e.getMessage(), "CustomerId", "", "Customer", e);

      e.printStackTrace();
    }
  }


}
