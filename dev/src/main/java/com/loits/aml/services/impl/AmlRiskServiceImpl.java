package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.CustomerProduct;
import com.loits.aml.domain.Transaction;
import com.loits.aml.repo.CustomerRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.AmlRiskService;
import com.redhat.aml.*;
import com.redhat.aml.sample.Customer;
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
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.loits.aml.domain.QCustomerRisk.customerRisk;

@Service
public class AmlRiskServiceImpl implements AmlRiskService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ModuleRepository moduleRepository;

    private static KieServicesConfiguration conf;
    private static KieServicesClient kieServicesClient;
    private static final MarshallingFormat FORMAT = MarshallingFormat.JSON;

    @Override
    public Object calcRisk(String nic, String user, Timestamp timestamp) throws FXDefaultException {

        ObjectMapper objectMapper = new ObjectMapper();
        HttpResponse httpResponse = null;
        String content = null;
        String jsonString = null;
        RestResponsePage restResponsePage = null;
        List<Transaction> transactionList = null;
        List<Customer> customerList = null;
        List<CustomerProduct> customerProductList = null;

        if (customerRepository.existsByNic(nic) || customerRepository.existsByOldNic(nic)) {

            URIBuilder builder;
            String url= null;

            //Get Customer from Customer Service
            try {
                builder = new URIBuilder("http://localhost:8091/aml-customer/v1/AnRkr?projection");
                builder.addParameter("oldNic", nic); //TODO get the customer
                url = builder.build().toString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            httpResponse = sendGetRequest(url, "Customer");
            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                restResponsePage = objectMapper.readValue(jsonString, RestResponsePage.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            customerList = restResponsePage.getContent();
            Customer customer = objectMapper.convertValue(customerList.get(0), Customer.class);

            //TODO set Customer details to a CustomerOnboarding object
            OnboardingCustomer onboardingCustomer = new OnboardingCustomer();
            onboardingCustomer.setId(customer.getId());
            onboardingCustomer.setClientCategory(customer.getClientCategory());
            onboardingCustomer.setAnnualTurnover(customer.getAnnualTurnover());
            onboardingCustomer.setAddressesByCustomerCode((Collection<Address>)customer.getAddressesByCustomerCode());
            onboardingCustomer.setCustomerType(customer.getCustomerType().getCode());
            onboardingCustomer.setIndustry(customer.getIndustry().getIsoCode());
            onboardingCustomer.setOccupation(customer.getOccupation().getIsoCode());
            onboardingCustomer.setModule(customer.getModule().getCode());
            onboardingCustomer.setPepsEnabled(customer.getPepsEnabled());
            onboardingCustomer.setWithinBranchServiceArea(customer.getWithinBranchServiceArea());

            CustomerRisk customerRisk = calculateCustomerCategoryRisk(onboardingCustomer);

            //Get the transactions for customer
            try {
                builder = new URIBuilder("http://localhost:8090/aml/transaction/v1/AnRkr?projection");

                builder.addParameter("customer.oldNic", nic); //TODO - Needs to be changed depending on req Might change with cus_product
                url = builder.build().toString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            httpResponse = sendGetRequest(url, "Aml");
            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                restResponsePage = objectMapper.readValue(jsonString, RestResponsePage.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            transactionList = objectMapper.convertValue(restResponsePage.getContent(), new TypeReference<List<Transaction>>() { });

            //send Transaction list to ChannelRisk
            List<ChannelUsage> channelUsageList = new ArrayList<>();


            for(Transaction t: transactionList){
                ChannelUsage channelUsage = new ChannelUsage();
                channelUsage.setDate(t.getTxnDate());
                channelUsage.setChannel(t.getChannel().getCode());
                channelUsageList.add(channelUsage);
            }
            ChannelRisk channelRisk = new ChannelRisk();
            channelRisk.setModule(customer.getModule().getCode());
            channelRisk.setChannelUsage(channelUsageList);
            channelRisk.setCustomerCode(customer.getId());
            channelRisk.setToday(timestamp);

            httpResponse = sendPostRequest(channelRisk, "http://localhost:8096/aml-channel-risk/v1/AnRkr?projection&timestamp=2019-05-29%2013%3A00%3A14", "ChannelRisk" );

            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                channelRisk = objectMapper.readValue(jsonString, ChannelRisk.class);
            } catch (IOException e) {
                e.printStackTrace();
            }


            //Get Customer Products
            try {
                builder = new URIBuilder("http://localhost:8090/aml/customer-product/v1/AnRkr?projection");

                builder.addParameter("customer.oldNic", nic); //TODO - Needs to be changed depending on req Might change with cus_product
                url = builder.build().toString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            httpResponse = sendGetRequest(url, "Aml");
            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                restResponsePage = objectMapper.readValue(jsonString, RestResponsePage.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            customerProductList = objectMapper.convertValue(restResponsePage.getContent(), new TypeReference<List<CustomerProduct>>() { });

            ProductRisk productRisk = new ProductRisk();
            productRisk.setCustomerCode(customer.getId());
            productRisk.setModule(customer.getModule().getCode());
            productRisk.setToday(new Date());

            List<Product> productList = new ArrayList<>();
            for (CustomerProduct cp :customerProductList) {
                Product product = new Product();
                product.setProductName(cp.getProduct().getCode());
                product.setCommencedDate(cp.getCommenceDate());
                product.setTerminatedDate(cp.getTerminateDate());
                product.setInterestRate(cp.getRate());
                product.setValue(cp.getValue());
                //product.setDefaultRate(cp.getProduct().getDefaultRate());  //TODO uncomment when data added
                product.setDefaultRate(1.0);
                List<com.redhat.aml.Transaction> ruleTransactionsList = new ArrayList<>();
                for (Transaction tr: cp.getTransactions()) {
                    com.redhat.aml.Transaction transaction = new com.redhat.aml.Transaction();
                    transaction.setType(tr.getTxnType());
                    transaction.setAmount(tr.getAmount());
                    transaction.setDate(tr.getTxnDate());
                    ruleTransactionsList.add(transaction);
                }
                product.setTransactions(ruleTransactionsList);
                productList.add(product);
            }
            productRisk.setProducts(productList);

            httpResponse = sendPostRequest(productRisk, "http://localhost:8095/aml-product-risk/v1/AnRkr?projection", "ProductRisk" );
            try {
                jsonString = EntityUtils.toString(httpResponse.getEntity());
                productRisk = objectMapper.readValue(jsonString, ProductRisk.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            OverallRisk overallRisk = new OverallRisk(onboardingCustomer.getId(), onboardingCustomer.getModule(), customerRisk.getCalculatedRisk(), productRisk.getCalculatedRisk(), channelRisk.getCalculatedRisk(), customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk());

            return calculateOverallRisk(overallRisk);

        }


        return transactionList;
    }

    @Override
    public Object calcOnboardingRisk(OnboardingCustomer onboardingCustomer, String user, Timestamp timestamp) throws FXDefaultException {

        CustomerRisk customerRisk = calculateCustomerCategoryRisk(onboardingCustomer);
        OverallRisk overallRisk = new OverallRisk(onboardingCustomer.getId(), onboardingCustomer.getModule(), customerRisk.getCalculatedRisk(), 0.0, 0.0, customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk());
        return calculateOverallRisk(overallRisk);
    }

    public CustomerRisk calculateCustomerCategoryRisk(OnboardingCustomer customer) throws FXDefaultException {
        HttpResponse httpResponse = sendPostRequest(customer, "http://localhost:8099/aml-category-risk/v1/AnRkr?projection", "Aml-Category-Risk");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;
        CustomerRisk customerRisk = null;
        try {
            jsonString = EntityUtils.toString(httpResponse.getEntity());
            customerRisk = objectMapper.readValue(jsonString, CustomerRisk.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return customerRisk;
    }

    public OverallRisk calculateOverallRisk(OverallRisk overallRisk) {
        conf = KieServicesFactory.newRestConfiguration("http://130.61.87.156:8080/kie-server/services/rest/server", "capi", "apic123");
        conf.setMarshallingFormat(FORMAT);
        kieServicesClient = KieServicesFactory.newKieServicesClient(conf);

        //Kie API
        String containerId = "AML_1.0.0-SNAPSHOT";
        System.out.println("== Sending commands to the server ==");
        RuleServicesClient rulesClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
        KieCommands commandsFactory = KieServices.Factory.get().getCommands();

        BatchExecutionCommandImpl command = new BatchExecutionCommandImpl();
        command.setLookup("kie-session");

        InsertObjectCommand insertObjectCommand = new InsertObjectCommand(overallRisk);
        FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();

        command.addCommand(insertObjectCommand);
        command.addCommand(fireAllRulesCommand);
        command.addCommand(commandsFactory.newGetObjects("OverallRisk"));

        ServiceResponse<ExecutionResults> response = rulesClient.executeCommandsWithResults(containerId, command);
        ArrayList obj = (ArrayList) response.getResult().getValue("OverallRisk");
        System.out.println();
        OverallRisk calculatedOverallRisk = (OverallRisk) obj.get(0);
        return calculatedOverallRisk;
    }


    public HttpResponse sendPostRequest(Object object, String url, String service) throws FXDefaultException {
        ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
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

        try {
            response = client.execute(httpReq);
        } catch (IOException e) {
            throw new FXDefaultException("", "Rest Request to " + service + " Failed", e.getMessage(), new Date(), HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    public HttpResponse sendGetRequest(String url, String service) throws FXDefaultException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = null;

        HttpGet httpReq = new HttpGet(url);
        httpReq.setHeader("Content-type", "application/json");

        try {
            response = client.execute(httpReq);
        } catch (IOException e) {
            throw new FXDefaultException("", "Rest Request to " + service + " Failed", e.getMessage(), new Date(), HttpStatus.BAD_REQUEST);
        }
        return response;
    }
}
