package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.Transaction;
import com.loits.aml.repo.CustomerRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.AmlRiskService;
import com.redhat.aml.Customer;
import com.redhat.aml.CustomerRisk;
import com.redhat.aml.OnboardingCustomer;
import com.redhat.aml.OverallRisk;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public Object calcRisk(Customer customer, String user, Timestamp timestamp) throws FXDefaultException {

        ObjectMapper mapper = new ObjectMapper();

        List<Transaction> transactionList=null;
        HttpResponse httpResponse=null;
        InputStream inputStream=null;
        String content=null;
        PageImpl page;
                //if (customerRepository.existsByNic(customer.getNic()) || customerRepository.existsByOldNic(customer.getNic())) {

           httpResponse = sendGetRequest("http://localhost:8090/aml/transaction/v1/AnRkr?projection", "Aml");
           HttpEntity entity = httpResponse.getEntity();

        try {
            //inputStream =  httpResponse.getEntity().getContent();
            content = EntityUtils.toString(entity);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            page =  mapper.readValue(content, PageImpl.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//
//            //String content = IOUtils.toString(httpResponse.getEntity().getContent()), StandardCharsets.UTF_8);
//
////            Transaction transaction = mapper.readValue(EntityUtils.toString(httpResponse.getEntity());
//           // transactionList = mapper.readValue(content, Object.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //}



      return transactionList;
    }

    @Override
    public Object calcOnboardingRisk(OnboardingCustomer customer, String user, Timestamp timestamp) throws FXDefaultException {
        HttpResponse httpResponse = sendPostRequest(customer, "http://localhost:8099/aml-category-risk/v1/AnRkr?projection", "Aml-Category-Risk");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString=null;
        CustomerRisk customerRisk=null;
        try {
            jsonString = EntityUtils.toString(httpResponse.getEntity());
            customerRisk = objectMapper.readValue(jsonString, CustomerRisk.class);
        } catch (IOException e) {
            e.printStackTrace();
        }



        //TODO send request to AML Risk

        OverallRisk overallRisk = new OverallRisk(1L,"Lending", customerRisk.getCalculatedRisk(), 0.0, 0.0, customerRisk.getPepsEnabled(), customerRisk.getCustomerType().getHighRisk(), customerRisk.getOccupation().getHighRisk() );



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
        OverallRisk overallRisk1 = (OverallRisk) obj.get(0);

        return overallRisk1;
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

        //Print status
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
        //Print status
        return response;
    }
}
