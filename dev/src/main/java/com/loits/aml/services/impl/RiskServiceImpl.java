package com.loits.aml.services.impl;

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
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
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
    CalcStatusRepository calcStatusRepository;

    @Autowired
    KieService kieService;

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    GeoLocationRepository geoLocationRepository;

    @Autowired
    SegmentedRiskService segmentedRiskService;

    @Autowired
    HTTPService httpService;

    @Value("${loits.tp.size}")
    int THREAD_POOL_SIZE;

    @Value("${loits.tp.queue.size}")
    int THREAD_POOL_QUEUE_SIZE;

    @Value("${aml.api.risk-calculate}")
    String AML_RISK_CALC_URL;

    @Value("${aml.risk-calculation.segment-size}")
    int SEGMENT_SIZE;

    @Value("${aml.risk-calculation.parallel-count}")
    int PARALLEL_SERVICE_SIZE;

    @Value("${default.risk-calc.sync.skip-interval}")
    private int DEFAULT_RISK_CALC_SKIP_HOURS;

    RandomStringGenerator randomStringGenerator;

    @PostConstruct
    public void init() {
        randomStringGenerator =
                new RandomStringGenerator.Builder()
                        .withinRange('0', 'z')
                        .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                        .build();
    }

    @Override
    public CompletableFuture<?> calculateRiskForCustomerBase(String projection, String user, String tenent,
                                                             RiskCalcParams riskCalcParams) {
        return CompletableFuture.runAsync(() -> {
            try {
                TenantHolder.setTenantId(tenent);

                if (projection.equals("initiate") || projection.equals("initiate --f")) {
                    this.initiateRiskCalculation(projection, user, tenent, riskCalcParams);
                } else if (projection.equals("calculate")) {
                    this.calculateRiskForServiceSegment(projection, user, tenent, riskCalcParams);
                }else {
                    throw new FXDefaultException("-1", "INVALID_ATTEMPT", "Not a supported risk calculation projection",
                            new Date(), HttpStatus.BAD_REQUEST, false);
                }
            } catch (Exception e) {
                logger.error("Customer Risk Calculation process error");
                e.printStackTrace();
            } finally {
                // clear tenant
                TenantHolder.clear();
            }
        });
    }


    /**
     * Calculating risk for a given customer segment is done here.
     * <p>
     * E.g.
     * <p>
     * When risk calculation service is called with page=2, size=1000 risk calculation, this method
     * will take the 2nd 1000 block and process the same with internal segmentation logic.
     *
     * @param projection
     * @param user
     * @param tenent
     * @param riskCalcParams
     */
    private void calculateRiskForServiceSegment(String projection, String user, String tenent,
                                                RiskCalcParams riskCalcParams) {
        logger.debug(String.format("%s - Risk calculation service segment process started", tenent));

        HashMap<String, Object> meta = new HashMap<>();
        List<CompletableFuture<?>> futuresList = new ArrayList<>();

        Integer page = riskCalcParams.getPage();
        Integer offset = riskCalcParams.getOffset();
        Integer size = riskCalcParams.getSize();
        String calcGroup = riskCalcParams.getCalcGroup();
        int startPage = 0;

        meta.put("page", page);
        meta.put("size" , size);
        meta.put("segmentSize" , SEGMENT_SIZE);

        // LOG Calculation task to DB.
        CalcStatus thisCalc = this.calcStatusService.saveCalcStatus(tenent, new CalcStatus(),
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_INITIATED,
                CalcTypes.CUST_RISK_CALC, meta);

        thisCalc.setCalcGroup(calcGroup);


        int noOfAsyncTasks, skip = 0, pageSize = 0;


        // override no of pages if set in environment.
        // this is purely for debugging purposes.
        // set the respective enviroment variable to -1 to disable
        // this behaviour.
        if (riskCalcParams.getPageLimit() !=null && riskCalcParams.getPageLimit().intValue() != -1) {
            // found page number overriding values
            noOfAsyncTasks = riskCalcParams.getPageLimit().intValue();
            pageSize = riskCalcParams.getRecordLimit().intValue();
            logger.debug(String.format("%s - Override Params - No of segments : %s, Page size : %s",
                    tenent, noOfAsyncTasks, pageSize));
        }else{

            // derive default value
            if (size > SEGMENT_SIZE) {
                noOfAsyncTasks = size / SEGMENT_SIZE;
                pageSize = SEGMENT_SIZE;
            } else {
                pageSize = size;
                noOfAsyncTasks = 1;
            }
        }

        // adjustment segment pages based on the service page
        noOfAsyncTasks = page == 0 ? noOfAsyncTasks : ((noOfAsyncTasks * page) + noOfAsyncTasks);

        // need force overriding to handle parallel requests
        // Example 1
        // i.   There are 6000 records in the database and we are running
        //      2 parallel service calls  -> 6000 / 2. Let's assume segment size is 1000.
        // ii.  This segment will receive page size of 3000 with either page 0 or 1.
        // iii. If '1' is received as the page, we need to ignore first 3000 records in this
        //      risk segment.
        skip = noOfAsyncTasks * page;


        logger.debug(String.format("%s - Risk calculation service segment process params - " +
                "   No of Async Tasks : %s, Skip : %s, Page size : %s", tenent, noOfAsyncTasks, skip, pageSize));


        if (offset != null) {
            // handle last page data set. Add additional page to task list
            noOfAsyncTasks += 1;
            logger.debug(String.format("%s - service segment risk calculation. Last page included", tenent));
        }

        meta.put("skip" , skip);
        meta.put("noOfAsyncTasks" , noOfAsyncTasks);
        meta.put("pageSize" , pageSize);
        meta.put("offset" , offset);

        for (int i = skip; i < noOfAsyncTasks; i++) {
            // send customer fetch -- tenant, page, size
            futuresList.add(this.segmentedRiskService.calculateCustomerSegmentRisk(riskCalcParams,
                    thisCalc.getId(), user, tenent, i,
                    pageSize));
        }

        CompletableFuture.allOf(
                futuresList.toArray(new CompletableFuture[futuresList.size()]))
                .whenComplete((result, ex) -> {
                    try {
                        TenantHolder.setTenantId(tenent);
                        logger.debug(String.format(
                                "%s - Risk calculation service segmented finished. Page - %s, Size - %s",
                                tenent, page, size));
                        if (ex != null) {
                            ex.printStackTrace();
                            logger.error(String.format(
                                    "%s - Risk calculation service segmented error. Page - %s, Size - %s, Error - %s",
                                    tenent, page, size, ex.getMessage()));
                            this.calcStatusService.saveCalcStatus(tenent, thisCalc,
                                    String.valueOf(Thread.currentThread().getId()),
                                    CalcStatusCodes.CALC_ERROR,
                                    CalcTypes.CUST_RISK_CALC, meta);
                        } else {
                            logger.error(String.format(
                                    "%s - Risk calculation service segmented completed. Page - %s, Size - %s",
                                    tenent, page, size));
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
    }


    /**
     * When invoked this will initiate the risk calculation. Could be run in two modes. i. Debugging mode and ii. General
     * <p>
     * E.g. Debugging Mode
     * <p>
     * When run in debugging mode, it's designed to run in 1 parallel task. Accepted parameters are
     * <p>
     * parallelCount = 1
     * pageLimit  = 10  (No of pages to be considered for risk calculation)
     * skip = 2 (No of pages to skip for risk calculation)
     * recordLimit = 1000 (No of records per  page)
     * <p>
     * <p>
     * E.g. General Mode
     * <p>
     * When run in general mode, this method will check the total record count, allocated parallel task count  and initiate
     * segmented risk calculation by invoking self method (via REST call) to populate segmented risk calculation across
     * multiple services.
     *
     * @param projection
     * @param user
     * @param tenent
     * @param riskCalcParams
     * @throws Exception
     */
    private void initiateRiskCalculation(String projection, String user, String tenent,
                                         RiskCalcParams riskCalcParams) throws Exception {
        /*
            Risk calculation initialization process shall be run once
        */
        HashMap<String, Object> meta = new HashMap<>();

        logger.debug(String.format("%s - Risk calculation process started", tenent));
        List<CompletableFuture<?>> futuresList = new ArrayList<>();
        String calcGroup = randomStringGenerator.generate(12);

        CalcStatus lastCalc = this.calcStatusService.getLastCalculation(CalcTypes.CUST_RISK_CALC);

        // LOG Calculation task to DB.
        CalcStatus thisCalc = this.calcStatusService.saveCalcStatus(tenent, new CalcStatus(),
                String.valueOf(Thread.currentThread().getId()),
                CalcStatusCodes.CALC_INITIATED,
                CalcTypes.CUST_RISK_CALC, meta);

        thisCalc.setCalcGroup(calcGroup);

        // make sure there aren't any multiple requests logged durig
        // given interval
        int  hours = 0;
        if (lastCalc!=null) {
            long secs = (new Date().getTime() - lastCalc.getMDate().getTime()) / 1000;
            hours = (int) (secs / 3600);
        }

        meta.put("groupId", calcGroup);
        meta.put("projection", projection);
        meta.put("lastCheckedHr", String.valueOf(hours));

        logger.debug(String.format("%s - Risk calculation last process run in : %s", tenent, hours));

        // Make sure not a multiple request
        if (hours >= DEFAULT_RISK_CALC_SKIP_HOURS || projection.equalsIgnoreCase("initiate --f")) {

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
            int parallelTasks = 1;
            int pageSize = 0, offset = 0;

            logger.debug(String.format("%s - Risk calculation estimation data loaded. Total Records : %s",
                    tenent, totRecords));


            if (riskCalcParams.getParallelCount() != -1) {
                parallelTasks = riskCalcParams.getParallelCount();
            } else parallelTasks = PARALLEL_SERVICE_SIZE;

            // calculate page size
            if (totRecords > 1) {
                pageSize = totRecords / parallelTasks;
                offset = totRecords % parallelTasks;
            } else pageSize = 1;

            meta.put("fetched", 1);
            meta.put("totalCustomers", totRecords);
            meta.put("parallelTasks", parallelTasks); // index starts at 0


            logger.debug(String.format(
                    "%s - Risk calculation service segment params -  Parallel Tasks : %s, Page size: %s",
                    tenent, parallelTasks, pageSize));

            for (int i = 0; i < parallelTasks; i++) {

                parameters = new HashMap<>();

                // if last page, might need to make an adjustment
                if (i == (parallelTasks - 1) &&
                        totRecords > PARALLEL_SERVICE_SIZE  && offset > 0 ) {
                    parameters.put("offset", String.valueOf(offset));
                    meta.put("offset", String.valueOf(offset));
                }

                // make a risk calculation request
                parameters.put("projection", "calculate");
                parameters.put("size", String.valueOf(pageSize));
                parameters.put("page", String.valueOf(i));
                parameters.put("calcGroup", calcGroup);
                parameters.put("pageLimit", riskCalcParams.getPageLimit().toString());
                parameters.put("recordLimit", riskCalcParams.getRecordLimit().toString());
                parameters.put("calcCategoryRisk", String.valueOf(riskCalcParams.isCalcCategoryRisk()));
                parameters.put("calcProductRisk", String.valueOf(riskCalcParams.isCalcProductRisk()));
                parameters.put("calcChannelRisk", String.valueOf(riskCalcParams.isCalcChannelRisk()));

                // send risk calculation request
                futuresList.add(this.initRiskCalcRequest(parameters, tenent ));
            }

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
                            logger.debug(String.format(
                                    "%s - Risk calculation service segmented requests issued.", tenent));

                            if (ex != null) {
                                ex.printStackTrace();
                                logger.error(String.format(
                                        "%s - Risk calculation service segmented init error : %s", tenent, ex.getMessage()));
                                this.calcStatusService.saveCalcStatus(tenent, thisCalc,
                                        String.valueOf(Thread.currentThread().getId()),
                                        CalcStatusCodes.CALC_ERROR,
                                        CalcTypes.CUST_RISK_CALC, meta);
                            } else {
                                logger.error(String.format(
                                        "%s - Risk calculation service segmented init completed", tenent));
                                this.calcStatusService.saveCalcStatus(tenent, thisCalc,
                                        String.valueOf(Thread.currentThread().getId()),
                                        CalcStatusCodes.CALC_COMPLETED,
                                        CalcTypes.CUST_RISK_CALC, meta);
                            }
                        } catch (Exception e) {
                            logger.error("Risk calculation task completion logging error");
                            e.printStackTrace();
                        } finally {
                            // clear tenant
                            TenantHolder.clear();
                        }
                    });
        } else {
            logger.debug(String.format("%s - Duplicate request during given time interval", tenent));
            throw new Exception("Duplicate request during given time interval");
        }
    }

    CompletableFuture<?> initRiskCalcRequest(HashMap<String, String> parameters,  String tenent ) {
        return CompletableFuture.runAsync(() -> {
            HashMap<String, String> headers = new HashMap<>();
            try {
                httpService.sendData("Customer-risk",
                        String.format(AML_RISK_CALC_URL, tenent),
                        parameters, headers, Object.class, null);
                logger.debug(String.format("%s - Risk calculation HTTP request initiated", tenent));
            } catch (Exception e) {
                logger.error(String.format("%s - Risk calculation HTTP request initialization error", tenent));
                e.printStackTrace();
            }
        });
    }


    @Override
    public Object calcOnboardingRisk(OnboardingCustomer onboardingCustomer, String user,
                                     String tenent) throws FXDefaultException, IOException,
            ClassNotFoundException, URISyntaxException {

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
