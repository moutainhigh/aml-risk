package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.Transaction;
import com.loits.aml.repo.CustomerRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.RiskService;
import com.redhat.aml.Customer;
import com.redhat.aml.OverallRisk;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class RiskServiceImpl implements RiskService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @Override
    public Object calcRisk(Customer customer, String user, Timestamp timestamp) throws FXDefaultException {
        HttpResponse httpResponse=null;
       if (customerRepository.existsByNic(customer.getNic()) || customerRepository.existsByOldNic(customer.getNic())) {
           //TODO send request to AML to get Transactions
           //TODO send request to Customer to get Customer Data

           //TODO should be filtered by customer id
           httpResponse = sendGetRequest("http://localhost:8090/aml/transaction/v1/AnRkr?projection", "Aml");
           //TODO Send customer data to category risk
           //TODO send transactions to channel risk
           //TODO get customer's products from Customer Product ?which service
           //TODO sent product list to transactions
           //TODO Send products and transactions to product risk

           ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            Iterable<Transaction> transactionList= objectMapper.readValue(httpResponse.getEntity().getContent(), Transaction.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//           BufferedReader rd = null;
//           try {
//               rd = new BufferedReader(
//                       new InputStreamReader(httpResponse.getEntity().getContent()));
//           } catch (IOException e) {
//               e.printStackTrace();
//           }
//
//           StringBuffer result = new StringBuffer();
//           String line = "";
//           while (true) {
//               try {
//                   if (!((line = rd.readLine()) != null)) break;
//               } catch (IOException e) {
//                   e.printStackTrace();
//               }
//               result.append(line);
//           }
       }
        return httpResponse.getEntity();
    }

    public void getChannelUsages() {

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
