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
import com.loits.aml.repo.AmlRiskRepository;
import com.loits.aml.repo.GeoLocationRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.AmlRiskService;
import com.loits.aml.services.KieService;
import com.loits.aml.services.ServiceMetadataService;
import com.loits.fx.aml.*;
import com.loits.fx.aml.Module;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class AmlRiskServiceImpl implements AmlRiskService {

    @Value("${loits.aml.anrkr.category-risk.url-key}")
    private String CATEGORY_RISK_URL_KEY;

    @Value("${loits.aml.anrkr.module-customer.url-key}")
    private String MODULE_CUSTOMER_URL_KEY;

    @Value("${loits.aml.anrkr.product-risk.url-key}")
    private String PRODUCT_RISK_URL_KEY;

    @Value("${loits.aml.anrkr.channel-risk.url-key}")
    private String CHANNEL_RISK_URL_KEY;

    @Value("${loits.aml.anrkr.aml-transactions.url-key}")
    private String AML_TRANSACTIONS_URL_KEY;

    @Value("${loits.aml.anrkr.aml-customer-products.url-key}")
    private String CUSTOMER_PRODUCTS_URL_KEY;

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

    @Override
    public Object calcOnboardingRisk(OnboardingCustomer onboardingCustomer, String user, String tenent) throws FXDefaultException {
        ObjectMapper objectMapper = null;
        RiskCustomer riskCustomer = null;

        //Check if a module exists by sent module code
        if (moduleRepository.existsByCode(onboardingCustomer.getModule())) {
            riskCustomer = new RiskCustomer();

            //copy similar properties from onboarding customer to risk customer
            //set id field to ignore when copying
            HashSet<String> ignoreFields = new HashSet<String>();
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

            if(onboardingCustomer.getAddressesByCustomerCode()!=null){
                //Get addresses of onboarding customer
                for (Address address:onboardingCustomer.getAddressesByCustomerCode()) {
                    //If address has a district
                    if(address.getDistrict()!=null){
                        if(geoLocationRepository.existsByLocationName(address.getDistrict())) {

                            GeoLocation geoLocation = geoLocationRepository.findByLocationName(address.getDistrict()).get();
                            Address riskAddress1 = new Address();
                            com.loits.fx.aml.GeoLocation ruleGeoLocation = new com.loits.fx.aml.GeoLocation();

                            NullAwareBeanUtilsBean utilsBean = new NullAwareBeanUtilsBean();
                            ignoreFields.clear();
                            ignoreFields.add("parent");
                            utilsBean.setIgnoreFields(ignoreFields);
                            com.loits.fx.aml.GeoLocation tempGeoLocation = ruleGeoLocation;

                            do{
                                try {
                                    utilsBean.copyProperties(tempGeoLocation, geoLocation);
                                    geoLocation = geoLocation.getParent();
                                    if(geoLocation!=null) {
                                        tempGeoLocation.setParent(new com.loits.fx.aml.GeoLocation());
                                        tempGeoLocation = tempGeoLocation.getParent();
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }while(geoLocation!=null);

                            riskAddress1.setGeoLocation(ruleGeoLocation);
                            riskAddresses.add(riskAddress1);
                        }
                    }

                }
                riskCustomer.setAddressesByCustomerCode(riskAddresses);
            }

            //Find module sent with onboarding customer
            com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(onboardingCustomer.getModule()).get();

            Module ruleModule = new Module();
            ruleModule.setCode(dbModule.getCode());
            if (dbModule.getParent() != null) {
                Module ruleModuleParent = new Module();
                ruleModuleParent.setCode(dbModule.getParent().getCode());
                ruleModule.setParent(ruleModuleParent);
            }
            riskCustomer.setModule(ruleModule);
        } else {
            throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"), new Date(), HttpStatus.BAD_REQUEST, false);
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put("user", user);

        HttpResponse httpResponse = sendPostRequest(riskCustomer, serviceMetadataService.getServiceMetadata(CATEGORY_RISK_URL_KEY).getMetaValue(), "Aml-Category-Risk", headers);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            objectMapper = new ObjectMapper();
            String jsonString = null;
            CustomerRisk customerRisk = null;
            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                customerRisk = objectMapper.readValue(jsonString, CustomerRisk.class);
            } catch (IOException e) {
                throw new FXDefaultException();
            }
            OverallRisk overallRisk = new OverallRisk(riskCustomer.getId(), riskCustomer.getModule(), customerRisk.getCalculatedRisk(), 0.0, 0.0, customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk());
            return kieService.getOverallRisk(overallRisk);
        } else {
            throw new FXDefaultException();
        }
    }

    @Override
    public Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user, String tenent) throws FXDefaultException {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpResponse httpResponse = null;
        String content = null;
        String jsonString = null;
        List<Customer> customerList = null;
        Customer customer = null;

        Module ruleModule=null;
        if(!moduleRepository.existsById(module)){
            throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"), new Date(), HttpStatus.BAD_REQUEST, false);
        }else {
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
            String customerServiceUrl = serviceMetadataService.getServiceMetadata(MODULE_CUSTOMER_URL_KEY).getMetaValue();
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("moduleCustomerCode", customerCode);
            parameters.put("module.code", ruleModule.getCode());

            //Send request to Customer Service
            RestResponsePage customerResultPage = sendServiceRequest(customerServiceUrl, parameters, null, "Customer");

            try {
                moduleCustomerList = customerResultPage.getContent();
                moduleCustomer = objectMapper.convertValue(moduleCustomerList.get(0), ModuleCustomer.class);
                customer = moduleCustomer.getCustomer();
            } catch (Exception e) {
                throw new FXDefaultException("3001", "INVALID_ATTEMPT", "Customer not found", new Date(), HttpStatus.BAD_REQUEST);
            }
        }

        if(amlRiskRepository.existsByCustomer(customer.getId())){
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
        }else{
            throw new FXDefaultException("3003", "NO_DATA_AVAILABLE", Translator.toLocale("NO_RISK_DATA"), new Date(), HttpStatus.BAD_REQUEST, false);
        }

//        //Only for test purposes
//        Module ruleModule = new Module();
//        ruleModule.setCode("lending");
//        Module ruleModuleParent = new Module();
//        ruleModuleParent.setCode("lofc");
//        ruleModule.setParent(ruleModuleParent);

//        OverallRisk overallRisk = new OverallRisk(Long.parseLong(customerCode), ruleModule, 36.5, 24.7, 66.0,  true, false, true);
//        return kieService.getOverallRisk(overallRisk);
    }

    public OverallRisk runRiskCronJob(String customerCode, String module, String otherIdentity, String user, String tenent) throws FXDefaultException{
        ObjectMapper objectMapper = new ObjectMapper();
        HttpResponse httpResponse = null;
        String content = null;
        String jsonString = null;
        List<Customer> customerList = null;

        Module ruleModule=null;
        if(!moduleRepository.existsById(module)){
            throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"), new Date(), HttpStatus.BAD_REQUEST, false);
        }else{
            com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(module).get();
            ruleModule = new Module();
            ruleModule.setCode(dbModule.getCode());
            if (dbModule.getParent() != null) {
                Module ruleModuleParent = new Module();
                ruleModuleParent.setCode(dbModule.getParent().getCode());
                ruleModule.setParent(ruleModuleParent);
            }
        }

        CustomerRisk customerRisk = calculateCustomerRisk(customerCode, ruleModule, user);

        ChannelRisk channelRisk = calculateChannelRisk(customerRisk.getCustomerCode(),  ruleModule,  user);

        ProductRisk productRisk = calculateProductRisk(customerRisk.getCustomerCode(), ruleModule, user);

        if (customerRisk.getCalculatedRisk() != null) {
            if (channelRisk.getCalculatedRisk() == null) {
                channelRisk.setCalculatedRisk(0.0);
            }
            if (productRisk.getCalculatedRisk() == null) {
                productRisk.setCalculatedRisk(0.0);
            }
            OverallRisk overallRisk = new OverallRisk(customerRisk.getCustomerCode(), ruleModule, customerRisk.getCalculatedRisk(), productRisk.getCalculatedRisk(), channelRisk.getCalculatedRisk(), customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk());
            overallRisk = kieService.getOverallRisk(overallRisk);


            AmlRisk amlRisk = new AmlRisk();
            amlRisk.setCreatedOn(new Timestamp(new Date().getTime()));
            amlRisk.setCreatedBy(user);
            amlRisk.setRiskRating(overallRisk.getRiskRating());
            amlRisk.setCustomerRisk(overallRisk.getCustomerRisk());
            amlRisk.setChannelRisk(overallRisk.getChannelRisk());
            amlRisk.setProductRisk(overallRisk.getProductRisk());
            amlRisk.setRisk(overallRisk.getCalculatedRisk());
            amlRisk.setCustomerRiskId(customerRisk.getId());
            amlRisk.setChannelRiskId(channelRisk.getId());
            amlRisk.setProductRiskId(productRisk.getId());
            amlRisk.setTenent(tenent);

            amlRiskRepository.save(amlRisk);
            kafkaProducer.publishToTopic("aml-risk-create", amlRisk);

            return overallRisk;
        } else {
            throw new FXDefaultException();
        }
    }


    public CustomerRisk calculateCustomerRisk(String customerCode, Module ruleModule, String user) throws FXDefaultException {
        List<ModuleCustomer> moduleCustomerList = null;
        Customer customer = null;
        ModuleCustomer moduleCustomer = null;
        ObjectMapper objectMapper = new ObjectMapper();

        //Request parameters to Customer Service
        String customerServiceUrl = serviceMetadataService.getServiceMetadata(MODULE_CUSTOMER_URL_KEY).getMetaValue();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("moduleCustomerCode", customerCode);
        parameters.put("module.code", ruleModule.getCode());

        //Send request to Customer Service
        RestResponsePage customerResultPage = sendServiceRequest(customerServiceUrl, parameters, null, "Customer");

        try {
            moduleCustomerList = customerResultPage.getContent();
            moduleCustomer = objectMapper.convertValue(moduleCustomerList.get(0), ModuleCustomer.class);
            customer = moduleCustomer.getCustomer();
        } catch (Exception e) {
            throw new FXDefaultException();
        }

        //Set Customer details to a CustomerOnboarding object
        RiskCustomer riskCustomer = new RiskCustomer();

        try {
            riskCustomer.setId(customer.getId());
            for (CustomerMeta customerMeta : customer.getCustomerMetaList()) {
                if (customerMeta.getType().equalsIgnoreCase("clientCategory")) {
                    riskCustomer.setClientCategory(customerMeta.getValue());
                }
                if (customerMeta.getType().equalsIgnoreCase("pepsEnabled")) {
                    if(customerMeta.getValue().equalsIgnoreCase("Y")){
                        riskCustomer.setPepsEnabled(Byte.parseByte("1"));
                    }else{
                        riskCustomer.setPepsEnabled(Byte.parseByte("0"));
                    }
                }
                if (customerMeta.getType().equalsIgnoreCase("withinBranchServiceArea")) {
                    if(customerMeta.getValue().equalsIgnoreCase("Y")){
                        riskCustomer.setPepsEnabled(Byte.parseByte("1"));
                    }else{
                        riskCustomer.setPepsEnabled(Byte.parseByte("0"));
                    }
                }
            }

            riskCustomer.setAnnualTurnover(customer.getAnnualTurnover());
            riskCustomer.setAddressesByCustomerCode((Collection<Address>) customer.getAddresses());
            riskCustomer.setCustomerType(customer.getCustomerType().getCode());
            riskCustomer.setIndustry(customer.getIndustry().getIsoCode());
            riskCustomer.setOccupation(customer.getOccupation().getIsoCode());
            riskCustomer.setModule(ruleModule);
            riskCustomer.setIndustryId(customer.getIndustry().getId());
            riskCustomer.setCustomerTypeId(customer.getCustomerType().getId());
            riskCustomer.setOccupationId(customer.getOccupation().getId());
        } catch (Exception e) {
            throw new FXDefaultException();
        }
        HashMap<String, String> headers = new HashMap<>();
        headers.put("user", user);
        HttpResponse res = sendPostRequest(riskCustomer, serviceMetadataService.getServiceMetadata(CATEGORY_RISK_URL_KEY).getMetaValue(), "Aml-Category-Risk", headers);
        CustomerRisk customerRisk = null;
        try {
            String jsonString = EntityUtils.toString(res.getEntity());
            customerRisk = objectMapper.readValue(jsonString, CustomerRisk.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (customerRisk.getCalculatedRisk() == null) {
            throw new FXDefaultException();
        }
        return customerRisk;
    }

    public ProductRisk calculateProductRisk(Long customerId, Module ruleModule, String user) throws FXDefaultException {
        List<CustomerProduct> customerProductList = null;
        ObjectMapper objectMapper = new ObjectMapper();
        ProductRisk productRisk = new ProductRisk();

        //Get the products for customer
        //Request parameters to AML Service
        String amlServiceProductsUrl = serviceMetadataService.getServiceMetadata(CUSTOMER_PRODUCTS_URL_KEY).getMetaValue();
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
                    transaction.setType(tr.getTxnType());
                    transaction.setAmount(tr.getAmount());
                    transaction.setDate(tr.getTxnDate());
                    ruleTransactionsList.add(transaction);
                }
                product.setTransactions(ruleTransactionsList);
                productList.add(product);
            }
            productRisk.setProducts(productList);

            HttpResponse httpResponse = sendPostRequest(productRisk, serviceMetadataService.getServiceMetadata(PRODUCT_RISK_URL_KEY).getMetaValue(), "ProductRisk", null);
            try {
                String jsonString = EntityUtils.toString(httpResponse.getEntity());
                productRisk = objectMapper.readValue(jsonString, ProductRisk.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            productRisk.setCalculatedRisk(0.0);
        }

        return productRisk;
    }

    public ChannelRisk calculateChannelRisk(Long customerId, Module module, String user) throws FXDefaultException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Transaction> transactionList = null;
        ChannelRisk channelRisk = new ChannelRisk();

        //Get the transactions for customer
        //Request parameters to AML Service
        String amlServiceTransactionUrl = serviceMetadataService.getServiceMetadata(AML_TRANSACTIONS_URL_KEY).getMetaValue();
        HashMap<String, String> transactionParameters = new HashMap<>();
        transactionParameters.put("customer.id", customerId.toString());

        //Send request to Customer Service
        ArrayList<Object> list = sendServiceRequest2(amlServiceTransactionUrl, transactionParameters, null, "AML");
        try {
            transactionList = objectMapper.convertValue(list, new TypeReference<List<Transaction>>() {
            });
        } catch (Exception e) {
            throw new FXDefaultException();
        }

        if (transactionList.size() > 0) {
            //send Transaction list to ChannelRisk
            List<ChannelUsage> channelUsageList = new ArrayList<>();

            for (Transaction t : transactionList) {
                ChannelUsage channelUsage = new ChannelUsage();
                channelUsage.setChannelId(t.getChannel().getId());
                channelUsage.setChannel(t.getChannel().getCode());
                channelUsage.setChannelName(t.getChannel().getChannelName());
                channelUsage.setChannelDescription(t.getChannel().getChannelDescription());
                channelUsage.setDate(t.getTxnDate());
                channelUsage.setAmount(t.getAmount());

                channelUsageList.add(channelUsage);

            }
            channelRisk.setModule(module);
            channelRisk.setChannelUsage(channelUsageList);
            channelRisk.setCustomerCode(customerId);
            channelRisk.setToday(new Timestamp(new Date().getTime()));

            HashMap<String, String> headers = new HashMap<>();
            headers.put("user", user);
            HttpResponse httpResponse = sendPostRequest(channelRisk, serviceMetadataService.getServiceMetadata(CHANNEL_RISK_URL_KEY).getMetaValue(), "ChannelRisk", headers);

            try {
                String jsonString = EntityUtils.toString(httpResponse.getEntity());
                channelRisk = objectMapper.readValue(jsonString, ChannelRisk.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            channelRisk.setCalculatedRisk(0.0);
        }
        return channelRisk;
    }


    public HttpResponse sendPostRequest(Object object, String url, String service, HashMap<String, String> headers) throws FXDefaultException {
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

    public HttpResponse sendGetRequest(String url, String service, HashMap<String, String> headers) throws FXDefaultException {

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


    public RestResponsePage sendServiceRequest(String serviceUrl, HashMap<String, String> parameters, HashMap<String, String> headers, String service) throws FXDefaultException {
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
            throw new FXDefaultException("", "", "", new Date(), HttpStatus.BAD_REQUEST);
        }
    }

    public ArrayList sendServiceRequest2(String serviceUrl, HashMap<String, String> parameters, HashMap<String, String> headers, String service) throws FXDefaultException {
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
            throw new FXDefaultException("", "", "", new Date(), HttpStatus.BAD_REQUEST);
        }
    }
}
