package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.repo.CustomerRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.services.RiskService;
import com.redhat.aml.Customer;
import com.redhat.aml.OverallRisk;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.sql.Timestamp;

public class RiskServiceImpl implements RiskService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @Override
    public Object calcRisk(String projection, Customer customer, String user, Timestamp timestamp) {
        if(customerRepository.existsByNicAndModule(customer.getNic())|| customerRepository.existsByOldNicAndModule(customer.getNic())){
            //TODO get CustomerType, Occ, Ind values,

        }
        return null;
    }

    public void sendRequest(Object object, String url){
        ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpPost httpPost = new HttpPost("http://localhost:8100/aml/v1?tenent=AnRkr");
        HttpEntity stringEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Content-type", "application/json");

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = null;
        try {
            response = client.execute(httpPost);
        } catch (IOException e) {
            //throw new FXDefaultException("","NewNotification Failed", e.getMessage(), new Date(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //Print status
        System.out.println(response.getStatusLine());
    }
}
