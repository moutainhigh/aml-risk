package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.commons.CalcStatusCodes;
import com.loits.aml.commons.CalcTypes;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.domain.CalcStatus;
import com.loits.aml.domain.CalcTasks;
import com.loits.aml.dto.Customer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.CalcStatusRepository;
import com.loits.aml.services.AmlRiskService;
import com.loits.aml.services.CalcStatusService;
import com.loits.aml.services.HTTPService;
import com.loits.aml.services.RiskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RiskServiceImpl implements RiskService {

  Logger logger = LogManager.getLogger(RiskServiceImpl.class);

  @Autowired
  Environment env;

  @Autowired
  AmlRiskService amlRiskService;

  @Autowired
  CalcStatusService calcStatusService;

  @Autowired
  CalcStatusRepository calcStatusRepository;

  @Autowired
  HTTPService httpService;

  @Value("${loits.tp.size}")
  int THREAD_POOL_SIZE;

  @Value("${loits.tp.queue.size}")
  int THREAD_POOL_QUEUE_SIZE;


  @Override
  public CompletableFuture<?> calculateRiskForCustomerBase(String user, String tenent) {

    return CompletableFuture.runAsync(() -> {
      try {
        TenantHolder.setTenantId(tenent);
        List<CompletableFuture<?>> futuresList = new ArrayList<>();
        logger.debug("Customer base risk calculation process started");
        HashMap<String, Object> meta = new HashMap<>();

        // LOG Calculation task to DB.
        CalcStatus thisCalc = this.calcStatusService.saveCalcStatus(tenent, new CalcStatus(),
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_INITIATED,
                CalcTypes.CUST_RISK_CALC, meta);

        // fetch a single customer page to determine no of customer records.
        //Request parameters to Customer Service
        String customerServiceUrl = String.format(env.getProperty("aml.api.customer"), tenent);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("size", "1");
        parameters.put("projection", "");

        //Send request to Customer Service
        RestResponsePage customerResultPage =
                amlRiskService.sendServiceRequest(customerServiceUrl, parameters,
                        null, "Customer");

        int totRecords = customerResultPage.getTotalPages(); //Total pages = Total Customers
        int pageSize = 0;
        int totalThreadedTasks = (THREAD_POOL_QUEUE_SIZE * THREAD_POOL_SIZE);

        // calculate customer risk in segments.
        // Max. allowed segments --PARALLEL_THREADS
        int noOfAsyncTasks = 1;

        if (totRecords >= totalThreadedTasks) {
          noOfAsyncTasks = totRecords /totalThreadedTasks ;
          pageSize = noOfAsyncTasks;
        } else pageSize = totRecords;

        logger.debug(String.format("Task parameters. No of Async Tasks : %s, Page size : %s, " +
                "Total Records : %s", noOfAsyncTasks, pageSize, totRecords));

        meta.put("fetched", 1);
        meta.put("totalCustomers", pageSize);
        meta.put("tpSize", THREAD_POOL_SIZE);
        meta.put("tpQueueSize", THREAD_POOL_QUEUE_SIZE);

        for (int i = 0; i < noOfAsyncTasks; i++) {

          // if last page, might need to make an adjustment
          if (i == noOfAsyncTasks - 1 &&
                  totRecords >= totalThreadedTasks) {
            int orphanRecordCount = totRecords % totalThreadedTasks;
            pageSize += orphanRecordCount;
            meta.put("finalPageSize", pageSize);
          }

          // send customer fetch -- tenant, page, size
          futuresList.add(this.calculateCustomerRisk(thisCalc.getId(), user, tenent, i, pageSize));
        }

        meta.put("asyncTaskCount", futuresList.size());

        // LOG Calculation task to DB.
        this.calcStatusService.saveCalcStatus(tenent, thisCalc,
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_UPDATED,
                CalcTypes.CUST_RISK_CALC, meta);

        CompletableFuture.allOf(
                futuresList.toArray(new CompletableFuture[futuresList.size()]))
                .whenComplete((result, ex) -> {
                  if (ex != null) {
                    logger.debug("All customer risk calculations processes error");
                    this.calcStatusService.saveCalcStatus(tenent, thisCalc,
                            String.valueOf(Thread.currentThread().getId()),
                            CalcStatusCodes.CALC_ERROR,
                            CalcTypes.CUST_RISK_CALC, meta);
                  } else {
                    logger.debug("All customer risk calculations processes completed");
                    this.calcStatusService.saveCalcStatus(tenent, thisCalc,
                            String.valueOf(Thread.currentThread().getId()),
                            CalcStatusCodes.CALC_COMPLETED,
                            CalcTypes.CUST_RISK_CALC, meta);
                  }
                });

      } catch (Exception e) {
        logger.error("Customer Risk Calculation process error");
        e.printStackTrace();
      } finally {
        // clear tenant
        TenantHolder.clear();
      }
    });
  }

  private CompletableFuture<?> calculateCustomerRisk(Long calId, String user, String tenent,
                                                     int page,
                                                     int size) {
    return CompletableFuture.runAsync(() -> {

      TenantHolder.setTenantId(tenent);
      HashMap<String, Object> meta = new HashMap<>();

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
        parameters.put("page", "id,asc");
        parameters.put("size", String.valueOf(size));

        try {
          logger.debug("Sending request to Customer API to get Customer");
          customerList = httpService.getDataFromPage("Customer", customerServiceUrl, parameters,
                  new TypeReference<List<Customer>>() {
                  });
          logger.debug("Customers successfully retrieved");
        } catch (Exception e) {
          logger.debug("Customer retrieval failed with " + e.getMessage());
        }

        if (customerList != null && !customerList.isEmpty()) {
          meta.put("fetched", customerList.size());

          // update calc status
          this.calcStatusService.saveCalcTask(thisTask, calId,
                  String.valueOf(Thread.currentThread().getId()),
                  CalcStatusCodes.CALC_UPDATED, meta);

          for (Customer customer : customerList) {
            Boolean calculateCustRisk;
            if (customer.getRiskCalculationStatus() == null
                    || customer.getRiskCalculationStatus() == 0
                    || customer.getRiskCalculationStatus() != customer.getVersion()) {
              calculateCustRisk = true;
            } else {
              calculateCustRisk = false;
            }
            try {
              amlRiskService.runRiskCronJob(calculateCustRisk, user, tenent, customer);
              logger.debug("Risk calculated for customer with id " + customer.getId());
              //customer.setRiskCalculationStatus(customer.getVersion());
              successCount += 1;
            } catch (Exception e) {
              e.printStackTrace();
              this.calcStatusService.saveCalcLog(thisTask, "Customer risk calculation failed",
                      e.getMessage(), "CustomerId", String.valueOf(customer.getId()), "Customer",
                      e);
              logger.debug("Risk not calculated for customer id " + customer.getId());
              errorCount += 1;
            }
          }
        } else {
          logger.debug("Did not load any customers for risk calculation");
          meta.put("fetched", 0);
        }

        meta.put("saved", successCount);
        meta.put("errorCount", errorCount);

        // update calc status
        this.calcStatusService.saveCalcTask(thisTask, calId,
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_COMPLETED, meta);

        logger.debug(String.format("Risk calculation for page : %s completed", page));

      } catch (Exception e) {
        logger.error("Risk Calculation for segment - process error");

        // update calc status
        this.calcStatusService.saveCalcTask(thisTask, calId,
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_ERROR, meta);

        e.printStackTrace();
      } finally {
        // clear tenant
        TenantHolder.clear();
      }
    });
  }
}
