package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.commons.CalcStatusCodes;
import com.loits.aml.commons.CalcTypes;
import com.loits.aml.config.NullAwareBeanUtilsBean;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.config.Translator;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.CalcStatus;
import com.loits.aml.domain.CalcTasks;
import com.loits.aml.domain.GeoLocation;
import com.loits.aml.dto.Address;
import com.loits.aml.dto.Customer;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.aml.dto.RiskCustomer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.CalcStatusRepository;
import com.loits.aml.repo.GeoLocationRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.*;
import com.loits.fx.aml.CustomerRisk;
import com.loits.fx.aml.Module;
import com.loits.fx.aml.OverallRisk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class RiskServiceImpl implements RiskService {

  Logger logger = LogManager.getLogger(RiskServiceImpl.class);

  @Autowired
  Environment env;

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

  @Value("${loits.tp.size}")
  int THREAD_POOL_SIZE;

  @Value("${loits.tp.queue.size}")
  int THREAD_POOL_QUEUE_SIZE;

  @Value("${aml.risk-calculation.segment-size}")
  int SEGMENT_SIZE;


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
                httpService.sendServiceRequest(customerServiceUrl, parameters,
                        null, "Customer");

        int totRecords = customerResultPage.getTotalPages(); //Total pages = Total Customers
        int pageSize = 0;

        // calculate customer risk in segments.
        // Max. allowed segments --PARALLEL_THREADS
        int noOfAsyncTasks = 1;

        if (totRecords >= SEGMENT_SIZE) {
          noOfAsyncTasks = totRecords / SEGMENT_SIZE;
          pageSize = SEGMENT_SIZE;
        } else pageSize = totRecords;

        logger.debug(String.format("Task parameters. No of Async Tasks : %s, Page size : %s, " +
                "Total Records : %s", noOfAsyncTasks, pageSize, totRecords));

        meta.put("fetched", 1);
        meta.put("totalCustomers", totRecords);
        meta.put("noOfSegments", (noOfAsyncTasks + 1)); // index starts at 0
        meta.put("tpSize", THREAD_POOL_SIZE);
        meta.put("tpQueueSize", THREAD_POOL_QUEUE_SIZE);

        for (int i = 0; i <= noOfAsyncTasks; i++) {

          // if last page, might need to make an adjustment
          if (i == noOfAsyncTasks &&
                  totRecords >= SEGMENT_SIZE) {
            int orphanRecordCount = totRecords % SEGMENT_SIZE;
            pageSize = orphanRecordCount;
            meta.put("finalPageSize", pageSize);
          }

          // send customer fetch -- tenant, page, size
          futuresList.add(this.calculateCustomerSegmentRisk(thisCalc.getId(), user, tenent, i,
                  pageSize));
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
                  try {
                    TenantHolder.setTenantId(tenent);
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
                  } catch (Exception e) {
                    logger.error("Risk calcualtion task completion logging error");
                    e.printStackTrace();
                  } finally {
                    // clear tenant
                    TenantHolder.clear();
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

  @Override
  public Object calcOnboardingRisk(OnboardingCustomer onboardingCustomer, String user,
                                   String tenent) throws FXDefaultException, IOException,
          ClassNotFoundException {

    //Check if a module exists by sent module code
    if (!moduleRepository.existsByCode(onboardingCustomer.getModule())) {
      throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"),
              new Date(), HttpStatus.BAD_REQUEST, false);
    }

    //Create new object to be sent to Category Risk server for risk calculation
    RiskCustomer riskCustomer = new RiskCustomer();

    //Find module sent with onboarding customer
    com.loits.aml.domain.Module dbModule =
            moduleRepository.findByCode(onboardingCustomer.getModule()).get();

    Module ruleModule = new Module();
    ruleModule.setCode(dbModule.getCode());
    if (dbModule.getParent() != null) {
      Module ruleModuleParent = new Module();
      ruleModuleParent.setCode(dbModule.getParent().getCode());
      ruleModule.setParent(ruleModuleParent);
    }
    //set module to RiskCustomer object
    riskCustomer.setModule(ruleModule);

    //copy similar properties from onboarding customer to risk customer
    //set id field to ignore when copying
    HashSet<String> ignoreFields = new HashSet<String>();
    //ignore module field and addresses on property transfer (Similar reference but data type
    // different)
    ignoreFields.add("module");
    ignoreFields.add("addressesByCustomerCode");

    try {
      NullAwareBeanUtilsBean utilsBean = new NullAwareBeanUtilsBean();
      utilsBean.setIgnoreFields(ignoreFields);
      utilsBean.copyProperties(riskCustomer, onboardingCustomer);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    //List for creating onboarding customer Addresses by GeoLocation
    List<Address> riskAddresses = new ArrayList<>();

    if (onboardingCustomer.getAddressesByCustomerCode() != null) {
      //Get addresses of onboarding customer
      for (Address address : onboardingCustomer.getAddressesByCustomerCode()) {
        //If address has a district
        if (address.getDistrict() != null && geoLocationRepository.existsByLocationName(address.getDistrict())) {

          GeoLocation geoLocation =
                  geoLocationRepository.findTopByLocationName(address.getDistrict()).get();
          Address riskAddress1 = new Address();
          com.loits.fx.aml.GeoLocation ruleGeoLocation = new com.loits.fx.aml.GeoLocation();

          NullAwareBeanUtilsBean utilsBean = new NullAwareBeanUtilsBean();
          ignoreFields.clear();
          ignoreFields.add("parent");
          utilsBean.setIgnoreFields(ignoreFields);
          com.loits.fx.aml.GeoLocation tempGeoLocation = ruleGeoLocation;

          do {
            try {
              utilsBean.copyProperties(tempGeoLocation, geoLocation);
              geoLocation = geoLocation.getParent();
              if (geoLocation != null) {
                tempGeoLocation.setParent(new com.loits.fx.aml.GeoLocation());
                tempGeoLocation = tempGeoLocation.getParent();
              }
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            } catch (InvocationTargetException e) {
              e.printStackTrace();
            }
          } while (geoLocation != null);

          riskAddress1.setGeoLocation(ruleGeoLocation);
          riskAddresses.add(riskAddress1);
        } else {
          if (geoLocationRepository.existsByLocationName(address.getCountry())) {
            GeoLocation countryGeo =
                    geoLocationRepository.findTopByLocationName(address.getCountry()).get();
            Address riskAddress1 = new Address();
            com.loits.fx.aml.GeoLocation ruleGeoLocation = new com.loits.fx.aml.GeoLocation();

            NullAwareBeanUtilsBean utilsBean = new NullAwareBeanUtilsBean();
            ignoreFields.clear();
            ignoreFields.add("parent");
            utilsBean.setIgnoreFields(ignoreFields);
            try {
              utilsBean.copyProperties(ruleGeoLocation, countryGeo);
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            } catch (InvocationTargetException e) {
              e.printStackTrace();
            }

            riskAddress1.setGeoLocation(ruleGeoLocation);
            riskAddresses.add(riskAddress1);
          }
        }

      }
      riskCustomer.setAddressesByCustomerCode(riskAddresses);
    }

    //Headers for CategoryRisk POST Req
    HashMap<String, String> headers = new HashMap<>();
    headers.put("user", user);

    //Calculate customer category risk by sending request to Category Risk Service
    CustomerRisk customerRisk = (CustomerRisk) httpService.sendData("Category-risk",
            String.format(env.getProperty("aml.api.category-risk"), tenent),
            null, headers, CustomerRisk.class, riskCustomer);

    //Calculate overallrisk by sending request to rule-engine
    OverallRisk overallRisk = new OverallRisk(riskCustomer.getId(), riskCustomer.getModule(),
            customerRisk.getCalculatedRisk(), 0.0, 0.0, customerRisk.getPepsEnabled(),
            customerRisk.getCustomerType().getHighRisk(),
            customerRisk.getOccupation().getHighRisk());

    return kieService.getOverallRisk(overallRisk);
  }

  private CompletableFuture<?> calculateCustomerSegmentRisk(Long calId, String user, String tenent,
                                                            int page,
                                                            int size) {
    return CompletableFuture.runAsync(() -> {

      TenantHolder.setTenantId(tenent);
      HashMap<String, Object> meta = new HashMap<>();

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
        int errorCount = 0, successCount = 0, updatedCount = 0;

        //Request parameters to Customer Service
        String customerServiceUrl = String.format(env.getProperty("aml.api.customer"), tenent);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("page", String.valueOf(page));
        parameters.put("sort", "id,asc");
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
                  CalcStatusCodes.CALC_COMPLETED, meta);

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
              if (amlRiskService.runRiskCronJob(calculateCustRisk, user, tenent, customer)) {
                updatedCount += 1;
              }
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

        meta.put("processed", successCount);
        meta.put("updated", updatedCount);
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
