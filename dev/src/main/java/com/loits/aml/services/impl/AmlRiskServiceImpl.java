package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.config.NullAwareBeanUtilsBean;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.config.Translator;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.AmlRisk;
import com.loits.aml.domain.GeoLocation;
import com.loits.aml.dto.*;
import com.loits.aml.dto.Transaction;
import com.loits.aml.kafka.services.KafkaProducer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.AmlRiskRepository;
import com.loits.aml.repo.GeoLocationRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.AmlRiskService;
import com.loits.aml.services.HTTPService;
import com.loits.aml.services.KieService;
import com.loits.aml.services.ServiceMetadataService;
import com.loits.fx.aml.*;
import com.loits.fx.aml.CustomerType;
import com.loits.fx.aml.Module;
import com.loits.fx.aml.Occupation;
import com.loits.fx.aml.Product;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AmlRiskServiceImpl implements AmlRiskService {

    Logger logger = LogManager.getLogger(AmlRiskServiceImpl.class);

    @Autowired
    KieService kieService;

    @Autowired
    AmlRiskRepository amlRiskRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    ServiceMetadataService serviceMetadataService;

    @Autowired
    GeoLocationRepository geoLocationRepository;

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    Environment env;

    @Autowired
    HTTPService httpService;

    @Value("${global.date.format}")
    private String dateFormat;

    @Value("${aml.transaction.default.back-months}")
    private String DEFAULT_BACK_MONTHS_TRANSACTION;

    SimpleDateFormat sdf;

    @PostConstruct
    public void init() {
        this.sdf = new SimpleDateFormat(dateFormat);
    }

    @Override
    public Object calcOnboardingRisk(OnboardingCustomer onboardingCustomer, String user, String tenent) throws FXDefaultException, IOException, ClassNotFoundException {

        //Check if a module exists by sent module code
        if (!moduleRepository.existsByCode(onboardingCustomer.getModule())) {
            throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"), new Date(), HttpStatus.BAD_REQUEST, false);
        }

        //Create new object to be sent to Category Risk server for risk calculation
        RiskCustomer riskCustomer = new RiskCustomer();

        //Find module sent with onboarding customer
        com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(onboardingCustomer.getModule()).get();

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
        //ignore module field and addresses on property transfer (Similar reference but data type different)
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

                    GeoLocation geoLocation = geoLocationRepository.findTopByLocationName(address.getDistrict()).get();
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
                        GeoLocation countryGeo = geoLocationRepository.findTopByLocationName(address.getCountry()).get();
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
        CustomerRisk customerRisk = (CustomerRisk) httpService.sendData("Category-risk",String.format(env.getProperty("aml.api.category-risk"), tenent),
                null,headers,  CustomerRisk.class, riskCustomer );

        //Calculate overallrisk by sending request to rule-engine
        OverallRisk overallRisk = new OverallRisk(riskCustomer.getId(), riskCustomer.getModule(), customerRisk.getCalculatedRisk(), 0.0, 0.0, customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk());

        return kieService.getOverallRisk(overallRisk);
    }

    @Override
    public Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user, String tenent) throws FXDefaultException {
        ObjectMapper objectMapper = new ObjectMapper();
        Customer customer = null;

        //Find module from module table
        Module ruleModule = null;
        if (!moduleRepository.existsById(module)) {
            throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"), new Date(), HttpStatus.BAD_REQUEST, false);
        } else {
            //Build module
            com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(module).get();
            ruleModule = new Module();
            ruleModule.setCode(dbModule.getCode());
            if (dbModule.getParent() != null) {
                Module ruleModuleParent = new Module();
                ruleModuleParent.setCode(dbModule.getParent().getCode());
                ruleModule.setParent(ruleModuleParent);
            }

            List<ModuleCustomer> moduleCustomerList = null;
            ModuleCustomer moduleCustomer = null;

            //Request parameters to Customer Service
            String customerServiceUrl = String.format(env.getProperty("aml.api.customer-module-customer"), tenent);
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("moduleCustomerCode", customerCode);
            parameters.put("module.code", module);

            try {
                moduleCustomerList = httpService.getData("Customer", customerServiceUrl, parameters, new TypeReference<List<ModuleCustomer>>(){});
                moduleCustomer = objectMapper.convertValue(moduleCustomerList.get(0), ModuleCustomer.class);
                customer = moduleCustomer.getCustomer();
            } catch (Exception e) {
                throw new FXDefaultException("3001", "INVALID_ATTEMPT", "Customer not found", new Date(), HttpStatus.BAD_REQUEST);
            }
        }

        //Get last calculated risk of customer from AMLRisk table
        if (amlRiskRepository.existsByCustomer(customer.getId())) {
            AmlRisk amlRisk = amlRiskRepository.findTopByCustomerOrderByCreatedOnDesc(customer.getId()).get();
            OverallRisk overallRisk = new OverallRisk();
            overallRisk.setCustomerCode(amlRisk.getCustomer());
            overallRisk.setModule(ruleModule);
            overallRisk.setRiskRating(amlRisk.getRiskRating());
            overallRisk.setCalculatedRisk(amlRisk.getRisk());
            overallRisk.setChannelRisk(amlRisk.getChannelRisk());
            overallRisk.setProductRisk(amlRisk.getProductRisk());
            overallRisk.setCustomerRisk(amlRisk.getCustomerRisk());
            return overallRisk;
        } else {
            throw new FXDefaultException("3003", "NO_DATA_AVAILABLE", Translator.toLocale("NO_RISK_DATA"), new Date(), HttpStatus.BAD_REQUEST, false);
        }
    }


    public OverallRisk calculateRiskByCustomer(String user, String tenent, Long id) throws FXDefaultException {

        List<Customer> customerList = null;
        Customer customer = null;
        ObjectMapper objectMapper = new ObjectMapper();

        //Request parameters to Customer Service
        String customerServiceUrl = String.format(env.getProperty("aml.api.customer"), tenent);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", String.valueOf(id));

        try {
            customerList = httpService.getData("Customer", customerServiceUrl, parameters, new TypeReference<List<Customer>>(){});
            customer = objectMapper.convertValue(customerList.get(0), Customer.class);
        } catch (Exception e) {
            throw new FXDefaultException("-1", "NO_DATA_FOUND", "No customers found", new Date(), HttpStatus.BAD_REQUEST, false);
        }

        String module = "lending";//TODO customer.getCustomerModule().getModule();
        Module ruleModule = null;
        if (!moduleRepository.existsById(module)) {
            throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"), new Date(), HttpStatus.BAD_REQUEST, false);
        } else {
            com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(module).get();
            ruleModule = new Module();
            ruleModule.setCode(dbModule.getCode());
            if (dbModule.getParent() != null) {
                Module ruleModuleParent = new Module();
                ruleModuleParent.setCode(dbModule.getParent().getCode());
                ruleModule.setParent(ruleModuleParent);
            }

            CustomerRisk customerRisk = calculateCustomerRisk(customer, ruleModule, user, tenent);

            ChannelRisk channelRisk = calculateChannelRisk(customer.getId(), ruleModule, user, tenent);

            ProductRisk productRisk = calculateProductRisk(customer.getId(), ruleModule, user, tenent);

            if (customerRisk.getCalculatedRisk() != null) {
                if (channelRisk.getCalculatedRisk() == null) {
                    channelRisk.setCalculatedRisk(0.0);
                }
                if (productRisk.getCalculatedRisk() == null) {
                    productRisk.setCalculatedRisk(0.0);
                }

                if(customerRisk.getPepsEnabled()==null){
                    customerRisk.setPepsEnabled(false);
                }

                if(customerRisk.getCustomerType()==null){
                    CustomerType customerType = new CustomerType();
                    customerType.setHighRisk(false);
                    customerRisk.setCustomerType(customerType);
                }
                if(customerRisk.getOccupation()==null){
                    Occupation occupation = new Occupation();
                    occupation.setHighRisk(false);
                    customerRisk.setOccupation(occupation);
                }


                OverallRisk overallRisk = new OverallRisk(customer.getId(), ruleModule, customerRisk.getCalculatedRisk(), productRisk.getCalculatedRisk(), channelRisk.getCalculatedRisk(), customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk());
                overallRisk = kieService.getOverallRisk(overallRisk);

                //Save to calculated AmlRisk record to overallrisk
                saveRiskRecord(overallRisk, customerRisk.getId(), productRisk.getId(), channelRisk.getId(), tenent, user);

                return overallRisk;
            } else {
                throw new FXDefaultException();
            }
        }
    }


    public CustomerRisk calculateCustomerRisk(Customer customer, Module ruleModule, String user, String tenent) throws FXDefaultException {
        ObjectMapper objectMapper = new ObjectMapper();

        //Set Customer details to a CustomerOnboarding object
        RiskCustomer riskCustomer = new RiskCustomer();

        try {
            riskCustomer.setId(customer.getId());
            for (CustomerMeta customerMeta : customer.getCustomerMetaList()) {
                if (customerMeta.getType().equalsIgnoreCase("clientCategory")) {
                    riskCustomer.setClientCategory(customerMeta.getValue());
                }
                if (customerMeta.getType().equalsIgnoreCase("pepsEnabled")) {
                    if (customerMeta.getValue().equalsIgnoreCase("Y")) {
                        riskCustomer.setPepsEnabled(Byte.parseByte("1"));
                    } else {
                        riskCustomer.setPepsEnabled(Byte.parseByte("0"));
                    }
                }
                if (customerMeta.getType().equalsIgnoreCase("withinBranchServiceArea")) {
                    if (customerMeta.getValue().equalsIgnoreCase("Y")) {
                        riskCustomer.setWithinBranchServiceArea(Byte.parseByte("1"));
                    } else {
                        riskCustomer.setWithinBranchServiceArea(Byte.parseByte("0"));
                    }
                }
            }

            if(customer.getAnnualTurnoverFrom()!=null){
                riskCustomer.setAnnualTurnoverFrom(customer.getAnnualTurnoverFrom());
            }
            if(customer.getAnnualTurnoverTo()!=null){
                riskCustomer.setAnnualTurnoverTo(customer.getAnnualTurnoverTo());
            }
            if(customer.getAddresses()!=null){
                riskCustomer.setAddressesByCustomerCode((Collection<Address>) customer.getAddresses());
            }
            if(customer.getCustomerType()!=null){
                riskCustomer.setCustomerType(customer.getCustomerType().getCode());
                riskCustomer.setCustomerTypeId(customer.getCustomerType().getId());
            }

            if(customer.getIndustry()!=null){
                riskCustomer.setIndustry(customer.getIndustry().getIsoCode());
                riskCustomer.setIndustryId(customer.getIndustry().getId());
            }
            if(customer.getOccupation()!=null) {
                riskCustomer.setOccupation(customer.getOccupation().getIsoCode());
                riskCustomer.setOccupationId(customer.getOccupation().getId());
            }
            riskCustomer.setModule(ruleModule);
        } catch (Exception e) {
            logger.debug("Failure in adding data to customer category risk calculation model");
            throw new FXDefaultException("3001", "INVALID_ATTEMPT", "Incomplete Customer Category data for risk calculation", new Date(), HttpStatus.BAD_REQUEST, true);
        }

        //TODO change this to HTTP Service
        HashMap<String, String> headers = new HashMap<>();
        headers.put("user", user);
        HttpResponse res = sendPostRequest(riskCustomer, String.format(env.getProperty("aml.api.category-risk"), tenent), "Aml-Category-Risk", headers);
        CustomerRisk customerRisk = null;
        try {
            String jsonString = EntityUtils.toString(res.getEntity());
            customerRisk = objectMapper.readValue(jsonString, CustomerRisk.class);
            logger.debug("CustomerRisk calculated "+customerRisk);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (customerRisk.getCalculatedRisk() == null) {
            customerRisk.setCalculatedRisk(0.0);
        }
        return customerRisk;
    }


    public ChannelRisk calculateChannelRisk(Long customerId, Module module, String user, String tenent) throws
            FXDefaultException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Transaction> transactionList = null;
        ChannelRisk channelRisk = new ChannelRisk();

        //Get the transactions for customer
        //Request parameters to AML Service
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, Integer.parseInt(DEFAULT_BACK_MONTHS_TRANSACTION));

        String amlServiceTransactionUrl = String.format(env.getProperty("aml.api.aml-transactions"), tenent);
        HashMap<String, String> transactionParameters = new HashMap<>();
        transactionParameters.put("customerProduct.customer.id", customerId.toString());
        transactionParameters.put("txnDate", sdf.format(cal.getTime()));

        //Send request to Customer Service
        ArrayList<Object> list = sendServiceRequest2(amlServiceTransactionUrl, transactionParameters, null, "AML");
        try {
            transactionList = objectMapper.convertValue(list, new TypeReference<List<Transaction>>() {
            });
        } catch (Exception e) {
            channelRisk.setCalculatedRisk(0.0);
            return channelRisk;
        }

        if (transactionList.size() > 0) {
            //send Transaction list to ChannelRisk
            List<ChannelUsage> channelUsageList = new ArrayList<>();

            for (Transaction t : transactionList) {
                if(t.getChannel()!=null){
                    ChannelUsage channelUsage = new ChannelUsage();
                    channelUsage.setChannelId(t.getChannel().getId());
                    channelUsage.setChannel(t.getChannel().getCode());
                    channelUsage.setChannelName(t.getChannel().getChannelName());
                    channelUsage.setChannelDescription(t.getChannel().getChannelDescription());
                    channelUsage.setDate(t.getTxnDate());
                    channelUsage.setAmount(t.getTxnAmount().doubleValue());

                    channelUsageList.add(channelUsage);
                }
            }
            channelRisk.setModule(module);
            channelRisk.setChannelUsage(channelUsageList);
            channelRisk.setCustomerCode(customerId);
            channelRisk.setToday(new Timestamp(new Date().getTime()));

            //TODO add to HTTPService
            HashMap<String, String> headers = new HashMap<>();
            headers.put("user", user);
            HttpResponse httpResponse = sendPostRequest(channelRisk, String.format(env.getProperty("aml.api.channel-risk"), tenent), "ChannelRisk", headers);

            try {
                String jsonString = EntityUtils.toString(httpResponse.getEntity());
                channelRisk = objectMapper.readValue(jsonString, ChannelRisk.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            logger.debug("No transactions available to calculate Channel Risk. Aborting...");
            channelRisk.setCalculatedRisk(0.0);
        }
        return channelRisk;
    }

    public ProductRisk calculateProductRisk(Long customerId, Module ruleModule, String user, String tenent) throws
            FXDefaultException {
        List<CustomerProduct> customerProductList = null;
        ObjectMapper objectMapper = new ObjectMapper();
        ProductRisk productRisk = new ProductRisk();

        //Get the products for customer
        //Request parameters to AML Service
        String amlServiceProductsUrl = String.format(env.getProperty("aml.api.aml-customer-products"), tenent);
        HashMap<String, String> productsParameters = new HashMap<>();
        productsParameters.put("customer.id", customerId.toString());

        //Send request to Customer Service
        ArrayList list = sendServiceRequest2(amlServiceProductsUrl, productsParameters, null, "AML");

        customerProductList = objectMapper.convertValue(list, new TypeReference<List<CustomerProduct>>() {
        });

        if (customerProductList.size() > 0) {
            productRisk.setCustomerCode(customerId);
            productRisk.setModule(ruleModule);
            productRisk.setToday(new Date());

            List<Product> productList = new ArrayList<>();
            for (CustomerProduct cp : customerProductList) {
                Product product = new Product();
                product.setCode(cp.getProduct().getCode());
                product.setCommencedDate(cp.getCommenceDate());
                product.setTerminatedDate(cp.getTerminateDate());
                product.setInterestRate(cp.getRate());
                product.setValue(cp.getValue());
                product.setDefaultRate(cp.getProduct().getDefaultRate());
                List<com.loits.fx.aml.Transaction> ruleTransactionsList = new ArrayList<>();
                for (Transaction tr : cp.getTransactions()) {
                    com.loits.fx.aml.Transaction transaction = new com.loits.fx.aml.Transaction();
                    transaction.setType(tr.getTxnMode());
                    transaction.setAmount(tr.getTxnAmount().doubleValue());
                    transaction.setDate(tr.getTxnDate());
                    ruleTransactionsList.add(transaction);
                }
                product.setModule(cp.getProduct().getModule().getCode());
                product.setTransactions(ruleTransactionsList);
                productList.add(product);
            }
            productRisk.setProducts(productList);

            //TODO add to HTTPSERVice
            HttpResponse httpResponse = sendPostRequest(productRisk, String.format(env.getProperty("aml.api.product-risk"), tenent), "ProductRisk", null);
            try {
                String jsonString = EntityUtils.toString(httpResponse.getEntity());
                productRisk = objectMapper.readValue(jsonString, ProductRisk.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("No CustomerProducts available to calculate Product Risk. Aborting...");
            productRisk.setCalculatedRisk(0.0);
        }

        return productRisk;
    }


    public HttpResponse sendPostRequest(Object object, String url, String
            service, HashMap<String, String> headers) throws FXDefaultException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = null;

        HttpPost httpReq = new HttpPost(url);
        HttpEntity stringEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
        httpReq.setEntity(stringEntity);
        httpReq.setHeader("Content-type", "application/json");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpReq.setHeader(entry.getKey(), entry.getValue());
            }
        }

        try {
            response = client.execute(httpReq);
            return response;
        } catch (IOException e) {
            throw new FXDefaultException("", "Rest Request to " + service + " Failed", e.getMessage(), new Date(), HttpStatus.BAD_REQUEST);
        }
    }

    public HttpResponse sendGetRequest(String url, String service, HashMap<String, String> headers) throws
            FXDefaultException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = null;

        HttpGet httpReq = new HttpGet(url);
        httpReq.setHeader("Content-type", "application/json");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpReq.setHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            response = client.execute(httpReq);
        } catch (IOException e) {
            throw new FXDefaultException("", "Rest Request to " + service + " Failed", e.getMessage(), new Date(), HttpStatus.BAD_REQUEST);
        }
        return response;
    }


    @Override
    public RestResponsePage sendServiceRequest(String
                                                       serviceUrl, HashMap<String, String> parameters, HashMap<String, String> headers, String service) throws
            FXDefaultException {
        URIBuilder builder;
        String url = null;
        String jsonString = null;
        RestResponsePage restResponsePage = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            builder = new URIBuilder(serviceUrl);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
            url = builder.build().toString();
        } catch (URISyntaxException e) {
            throw new FXDefaultException();
        }

        HttpResponse httpResponse = sendGetRequest(url, service, headers);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                restResponsePage = objectMapper.readValue(jsonString, RestResponsePage.class);
            } catch (IOException e) {
                throw new FXDefaultException();
            }
            return restResponsePage;
        } else {
            throw new FXDefaultException("-1", "FAILED_REQUEST", "Service Request Failed to "+service, new Date(), HttpStatus.BAD_REQUEST);
        }
    }

    public ArrayList sendServiceRequest2(String
                                                 serviceUrl, HashMap<String, String> parameters, HashMap<String, String> headers, String service) throws
            FXDefaultException {
        URIBuilder builder;
        String url = null;
        String jsonString = null;
        ArrayList list = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            builder = new URIBuilder(serviceUrl);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
            url = builder.build().toString();
        } catch (URISyntaxException e) {
            throw new FXDefaultException();
        }

        HttpResponse httpResponse = sendGetRequest(url, service, headers);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                list = objectMapper.readValue(jsonString, ArrayList.class);
            } catch (IOException e) {
                throw new FXDefaultException();
            }
            return list;
        } else {
            throw new FXDefaultException("-1", "FAILED_REQUEST", "Service Request Failed to "+service, new Date(), HttpStatus.BAD_REQUEST);
        }
    }

    public void runRiskCronJob(String user, String tenent, Customer customer) throws FXDefaultException {

        String module = "lofc";//TODO customer.getCustomerModule().getModule();
        Module ruleModule = null;
        if (!moduleRepository.existsById(module)) {
            logger.debug("Failure in assigning module to customer in risk calculation");
        } else {
            com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(module).get();
            ruleModule = new Module();
            ruleModule.setCode(dbModule.getCode());
            if (dbModule.getParent() != null) {
                Module ruleModuleParent = new Module();
                ruleModuleParent.setCode(dbModule.getParent().getCode());
                ruleModule.setParent(ruleModuleParent);
            }

            CustomerRisk customerRisk = calculateCustomerRisk(customer, ruleModule, user, tenent);

            ChannelRisk channelRisk = calculateChannelRisk(customer.getId(), ruleModule, user, tenent);

            ProductRisk productRisk = calculateProductRisk(customer.getId(), ruleModule, user, tenent);

            if (customerRisk.getCalculatedRisk() != null) {
                if (channelRisk.getCalculatedRisk() == null) {
                    channelRisk.setCalculatedRisk(0.0);
                }
                if (productRisk.getCalculatedRisk() == null) {
                    productRisk.setCalculatedRisk(0.0);
                }
                if(customerRisk.getPepsEnabled()==null){
                    customerRisk.setPepsEnabled(false);
                }

                if(customerRisk.getCustomerType()==null){
                    CustomerType customerType = new CustomerType();
                    customerType.setHighRisk(false);
                    customerRisk.setCustomerType(customerType);
                }
                if(customerRisk.getOccupation()==null){
                    Occupation occupation = new Occupation();
                    occupation.setHighRisk(false);
                    customerRisk.setOccupation(occupation);
                }

                OverallRisk overallRisk = new OverallRisk(customer.getId(), ruleModule, customerRisk.getCalculatedRisk(), productRisk.getCalculatedRisk(), channelRisk.getCalculatedRisk(), customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk());

                overallRisk = kieService.getOverallRisk(overallRisk);

                //Save AMLRISK record
                saveRiskRecord(overallRisk, customerRisk.getId(), productRisk.getId(), channelRisk.getId(), tenent, user);

            } else {
                logger.debug("Failure in calculating risk for Customer with id "+customer.getId());
            }
        }
    }

    @Async
    CompletableFuture<?> saveRiskRecord(OverallRisk overallRisk, Long customerRiskId, Long productRiskId, Long channelRiskId, String tenent, String user) throws FXDefaultException{
        return CompletableFuture.runAsync(() -> {
            logger.debug("AmlRisk record save stared");
            TenantHolder.setTenantId(tenent);
            AmlRisk amlRisk = new AmlRisk();
            amlRisk.setCreatedOn(new Timestamp(new Date().getTime()));
            amlRisk.setCreatedBy(user);
            amlRisk.setRiskRating(overallRisk.getRiskRating());
            amlRisk.setCustomerRisk(overallRisk.getCustomerRisk());
            amlRisk.setChannelRisk(overallRisk.getChannelRisk());
            amlRisk.setProductRisk(overallRisk.getProductRisk());
            amlRisk.setRisk(overallRisk.getCalculatedRisk());
            amlRisk.setCustomerRiskId(customerRiskId);
            amlRisk.setChannelRiskId(channelRiskId);
            amlRisk.setProductRiskId(productRiskId);
            amlRisk.setTenent(tenent);
            amlRisk.setCustomer(overallRisk.getCustomerCode());
            StringBuilder stringBuilder = new StringBuilder();

            if (overallRisk.getPepsEnabled()) {
                stringBuilder.append("A politically exposed person");
            }
            if (overallRisk.getHighRiskCustomerType()) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(" with");
                } else {
                    stringBuilder.append("Customer has");
                }
                stringBuilder.append(" a high risk customer-type");
            } else {

            }
            if (overallRisk.getHighRiskOccupation()) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(" and");
                } else {
                    stringBuilder.append("Customer has");
                }
                stringBuilder.append(" a high risk occupation");
            }

            amlRisk.setRiskText(stringBuilder.toString());

            try {
                amlRisk = amlRiskRepository.save(amlRisk);
                logger.debug("AmlRisk record saved to database successfully");
            }catch (Exception e){
                logger.debug("AmlRisk record save failed");
            }
            amlRisk.setTenent(tenent);
            kafkaProducer.publishToTopic("aml-risk-create", amlRisk);
        });
    }
}
