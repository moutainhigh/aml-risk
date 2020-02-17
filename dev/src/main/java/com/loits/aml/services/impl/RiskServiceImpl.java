package com.loits.aml.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.commons.CalcStatusCodes;
import com.loits.aml.commons.CalcTypes;
import com.loits.aml.commons.RiskCalcParams;
import com.loits.aml.config.NullAwareBeanUtilsBean;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.config.Translator;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.CalcStatus;
import com.loits.aml.domain.GeoLocation;
import com.loits.aml.dto.Address;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.aml.dto.RiskCustomer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.CalcStatusRepository;
import com.loits.aml.repo.GeoLocationRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.*;
import com.loits.fx.aml.*;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
    SegmentedRiskService segmentedRiskService;

    @Autowired
    HTTPService httpService;

    @Value("${loits.tp.size}")
    int THREAD_POOL_SIZE;

    @Value("${loits.tp.queue.size}")
    int THREAD_POOL_QUEUE_SIZE;

    @Value("${aml.risk-calculation.segment-size}")
    int SEGMENT_SIZE;

    @Override
    public CompletableFuture<?> calculateRiskForCustomerBase(String user, String tenent,
                                                             RiskCalcParams riskCalcParams) {
        return CompletableFuture.runAsync(() -> {
            try {
                TenantHolder.setTenantId(tenent);
                List<CompletableFuture<?>> futuresList = new ArrayList<>();
                logger.info("Customer base risk calculation process started");
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
                boolean isDebugMode = false;

                // calculate customer risk in segments.
                // Max. allowed segments --PARALLEL_THREADS
                int noOfAsyncTasks = 1;
                int skip = 0;

                if (totRecords > SEGMENT_SIZE) {
                    noOfAsyncTasks = totRecords / SEGMENT_SIZE;
                    pageSize = SEGMENT_SIZE;
                } else pageSize = totRecords;


                // override no of pages if set in environment.
                // this is purely for testing purposes.
                // set the respective enviroment variable to -1 to disable
                // this behaviour.
                if (riskCalcParams.getPageLimit().intValue() != -1) {
                    // found page number overriding values
                    noOfAsyncTasks = riskCalcParams.getPageLimit().intValue();
                    isDebugMode = true;
                    logger.info("No of pages is overridden by environment value : No of segments : " + noOfAsyncTasks);

                    pageSize = riskCalcParams.getRecordLimit().intValue();
                }

                if (riskCalcParams.getSkip().intValue() != -1) {
                    skip = riskCalcParams.getSkip().intValue();
                    logger.info("Skip pages overridden by query paramvalue: " + skip);
                }

                logger.info(String.format("Task parameters. No of Async Tasks : %s, Page size : %s, " +
                        "Total Records : %s", noOfAsyncTasks, pageSize, totRecords));

                meta.put("fetched", 1);
                meta.put("totalCustomers", totRecords);
                meta.put("noOfSegments", noOfAsyncTasks); // index starts at 0
                meta.put("tpSize", THREAD_POOL_SIZE);
                meta.put("tpQueueSize", THREAD_POOL_QUEUE_SIZE);
                meta.put("calcParams", riskCalcParams.toString());

                for (int i = skip; i < noOfAsyncTasks; i++) {

                    // if last page, might need to make an adjustment
                    if (!isDebugMode && (i == (noOfAsyncTasks - 1) &&
                            totRecords > SEGMENT_SIZE)) {
                        int orphanRecordCount = totRecords % SEGMENT_SIZE;
                        pageSize = orphanRecordCount;
                        meta.put("finalPageSize", pageSize);
                    }

                    // send customer fetch -- tenant, page, size
                    futuresList.add(this.segmentedRiskService.calculateCustomerSegmentRisk(riskCalcParams,
                            thisCalc.getId(), user, tenent, i,
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

                    ObjectMapper objectMapper = new ObjectMapper();
                    com.loits.aml.dto.GeoLocation geoLocation1 = objectMapper.convertValue(ruleGeoLocation, com.loits.aml.dto.GeoLocation.class);
                    riskAddress1.setGeoLocation(geoLocation1);
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

                        ObjectMapper objectMapper = new ObjectMapper();
                        com.loits.aml.dto.GeoLocation geoLocation1 = objectMapper.convertValue(ruleGeoLocation, com.loits.aml.dto.GeoLocation.class);
                        riskAddress1.setGeoLocation(geoLocation1);
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

        if (customerRisk.getCalculatedRisk() == null) {
            customerRisk.setCalculatedRisk(0.0);
        }

        if (customerRisk.getPepsEnabled() == null) {
            customerRisk.setPepsEnabled(false);
        }

        if (customerRisk.getCustomerType() == null) {
            CustomerType customerType = new CustomerType();
            customerType.setHighRisk(false);
            customerRisk.setCustomerType(customerType);
        }
        if (customerRisk.getOccupation() == null) {
            Occupation occupation = new Occupation();
            occupation.setHighRisk(false);
            customerRisk.setOccupation(occupation);
        }

        //Calculate overallrisk by sending request to rule-engine
        OverallRisk overallRisk = new OverallRisk();
        overallRisk.setCustomerCode(riskCustomer.getId());
        overallRisk.setModule(riskCustomer.getModule());
        overallRisk.setCustomerRisk(customerRisk.getCalculatedRisk());
        overallRisk.setProductRisk(0.0);
        overallRisk.setChannelRisk(0.0);
        overallRisk.setPepsEnabled(customerRisk.getPepsEnabled());
        overallRisk.setHighRiskCustomerType(customerRisk.getCustomerType().getHighRisk());
        overallRisk.setHighRiskOccupation(customerRisk.getOccupation().getHighRisk());

        return kieService.getOverallRisk(overallRisk);
    }


    @Override
    public List<OverallRisk> calculateForModuleCustomers(String user, String tenent, List<OverallRisk> customers) throws FXDefaultException, ExecutionException, InterruptedException {

        final CompletableFuture<List<OverallRisk>> result = new CompletableFuture<>();
        List<CompletableFuture<OverallRisk>> futuresList = new ArrayList<>();

        customers.forEach(c -> {
            futuresList.add(amlRiskService.calcRiskForCustomer(c.getCustomerCode(), user, tenent, ""));
        });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futuresList.toArray(new CompletableFuture[futuresList.size()]));

        CompletableFuture<List<OverallRisk>> allCompletableFuture = allFutures.thenApply(future ->
                futuresList.stream()
                        .map(completableFuture -> completableFuture.join())
                        .collect(Collectors.toList())
        );

        allCompletableFuture.whenComplete((data, ex) -> {
            try {
                if (ex != null) {
                    logger.debug("CRP calculation for LVCR error");
                    result.completeExceptionally(ex);
                } else {
                    result.complete(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.completeExceptionally(e);
            }
        });

        logger.debug("Customer risk calculated");
        try {
            return result.join();
        } catch (Exception e) {
            e.printStackTrace();
            // throw async error
            throw new FXDefaultException("-1", "ERR", "Risk Calculation Error. Please see system log for details",
                    new Date(), HttpStatus.BAD_REQUEST, false);
        }
    }


}
