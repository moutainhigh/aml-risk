package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.commons.CalcStatusCodes;
import com.loits.aml.commons.CalcTypes;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.domain.CalcStatus;
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
import org.springframework.data.domain.Sort;
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

  @Value("${api.customer-risk-calculation-allowed-parallel-threads}")
  int PARALLEL_THREADS;

  @Value("${loits.tp.size}")
  int THREAD_POOL_SIZE;

  @Value("${loits.tp.queue.size}")
  int THREAD_POOL_QUEUE_SIZE;


  @Override
  public CompletableFuture<?> calculateRiskForCustomerBase(String user, String tenent) {

    return CompletableFuture.runAsync(() -> {
      try {
        Sort sort = new Sort(Sort.Direction.ASC, "id");
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
        parameters.put("sort", sort.toString());
        parameters.put("projection", "");

        //Send request to Customer Service
        RestResponsePage customerResultPage =
                amlRiskService.sendServiceRequest(customerServiceUrl, parameters,
                        null, "Customer");

        int totRecords = customerResultPage.getTotalPages(); //Total pages = Total Customers
        int pageSize = 0;

        // calculate customer risk in segments.
        // Max. allowed segments --PARALLEL_THREADS
        int noOfAsyncTasks = 1;

        if (totRecords >= (THREAD_POOL_QUEUE_SIZE * THREAD_POOL_SIZE)) {
          noOfAsyncTasks = totRecords / (THREAD_POOL_QUEUE_SIZE * THREAD_POOL_SIZE);
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
                  totRecords >= PARALLEL_THREADS) {
            int orphanRecordCount = totRecords % PARALLEL_THREADS;
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
                  } else logger.debug("All customer risk calculations processes completed");
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

      // Log sync status for this segment - init status
      CalcStatus thisCalc = this.calcStatusRepository.findById(calId).get();

      try {
        logger.debug(String.format("Starting to calculate risk for tenent : %s , page: %s , size:" +
                " %s", tenent, page, size));

        List<Customer> customerList = null;
        //Customer customer = null;
        ObjectMapper objectMapper = new ObjectMapper();

        //Request parameters to Customer Service
        String customerServiceUrl = String.format(env.getProperty("aml.api.customer"), tenent);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("page", String.valueOf(page));
        parameters.put("size", String.valueOf(size));

        try {
          logger.debug("Sending request to Customer API to get Customer");
          customerList = httpService.getDataFromPage("Customer", customerServiceUrl, parameters,
                  new TypeReference<List<Customer>>() {
                  });
//          customer = objectMapper.convertValue(customerList.get(0), Customer.class);
          logger.debug("Customers successfully retrieved");
        } catch (Exception e) {
          logger.debug("Customer retrieval failed with " + e.getMessage());
        }

        for (Customer customer : customerList) {
          Boolean calculateCustRisk;
          if (customer.getRiskCalculationStatus() == 0 || customer.getRiskCalculationStatus() != customer.getVersion()) {
            calculateCustRisk = true;
          } else {
            calculateCustRisk = false;
          }
          try {
            amlRiskService.runRiskCronJob(calculateCustRisk, user, tenent, customer);
            logger.debug("Risk calculated for customer with id " + customer.getId());
            //customer.setRiskCalculationStatus(customer.getVersion());
          } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Risk not calculated for customer id " + customer.getId());
          }
        }

        // Log sync status for this segment - completed status
//        syncStatusService.updateSyncStatus(thisSync, CalcStatusCodes.SYNC_COMPLETED);

        logger.debug(String.format("Risk calculation for page : %s completed", page));

      } catch (Exception e) {
        logger.error("Risk Calculation for segment - process error");

        // Log sync status for this segment - error status
//        syncStatusService.updateSyncStatus(thisSync, CalcStatusCodes.SYNC_ERROR);

        e.printStackTrace();
      } finally {
        // clear tenant
        TenantHolder.clear();
      }
    });
  }
}
