package com.loits.aml.services.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.services.HTTPService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

@Service
public class HttpServiceImpl implements HTTPService {

  Logger logger = LogManager.getLogger(HttpServiceImpl.class);

  private final String template1 = "Connection to external API failed ( %s). " +
          "Returned error code. ( %s)";
  private final String template2 = "REST API request failed. ( %s)";
  private final String template3 = "REST API request successful. Service : %s";

  @Override
  public <T, classType, binderType> List<classType> getDataFromPage(String key, String url,
                                                                    Map<String, String> queryParam,
                                                                    TypeReference typeReference)
          throws FXDefaultException, ClassNotFoundException {

    HttpResponse httpResponse;
    String jsonString;
    ObjectMapper objectMapper = new ObjectMapper();
    List<classType> data = null;
    url = this.buildGetURL(url, queryParam, key);

    // make sure valid URL
    if (url == null) throw new FXDefaultException("-1", "URL_BUILD_FAILED",
            "URL Build failed. (#" + key + ")", new Date(), HttpStatus.BAD_REQUEST);

    //Get Customer Products
    httpResponse = sendGetRequest(url, key, null);
    try {

      jsonString = EntityUtils.toString(httpResponse.getEntity());
      RestResponsePage page = objectMapper.readValue(jsonString, RestResponsePage.class);
      data = objectMapper.convertValue(page.getContent(), typeReference);
    } catch (IOException e) {
      logger.error("Error in response to RestResponsePage conversion");
      e.printStackTrace();
    }
    return data;
  }

  @Override
  public <T, classType, binderType> List<classType> getDataFromList(String key, String url,
                                                                    Map<String, String> queryParam,
                                                                    TypeReference typeReference)
          throws FXDefaultException, ClassNotFoundException {

    HttpResponse httpResponse;
    String jsonString;
    ObjectMapper objectMapper = new ObjectMapper();
    List<classType> data = null;
    url = this.buildGetURL(url, queryParam, key);

    // make sure valid URL
    if (url == null) throw new FXDefaultException("-1", "URL_BUILD_FAILED",
            "URL Build failed. (#" + key + ")", new Date(), HttpStatus.BAD_REQUEST);

    //Get Customer Products
    httpResponse = sendGetRequest(url, key, null);
    try {

      jsonString = EntityUtils.toString(httpResponse.getEntity());
      ArrayList list = objectMapper.readValue(jsonString, ArrayList.class);
      data = objectMapper.convertValue(list, typeReference);
    } catch (IOException e) {
      logger.error("Error in response to RestResponsePage conversion");
      e.printStackTrace();
    }
    return data;
  }

  @Override
  public <T, classType, binderType> T sendData(String key, String url,
                                               Map<String, String> queryParam, HashMap<String,
          String> headers, Class classType, Object object) throws FXDefaultException, IOException
          , ClassNotFoundException, URISyntaxException {
    HttpResponse httpResponse;
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    String jsonString;
    Object responseEntity = null;
    URIBuilder builder  = new URIBuilder(url);

    if (queryParam != null && !queryParam.isEmpty())
      queryParam.entrySet().forEach(x -> builder.addParameter(x.getKey(), x.getValue()));


    httpResponse = sendPostRequest(builder.build().toString(), key, headers, object);

    try {
      jsonString = EntityUtils.toString(httpResponse.getEntity());
      responseEntity = objectMapper.readValue(jsonString, classType);
    } catch (IOException e) {
      logger.error("Error in response to Object conversion");
    }

    return (T) responseEntity;
  }

//  @Override
//  public HttpResponse sendEmail(String key, String url, Map<String, String> queryParam, String
// method, Object requestEntity) throws FXDefaultException, IOException, ClassNotFoundException {
//    ObjectMapper objectMapper = new ObjectMapper();
//    String emailJson = null;
//    try {
//      emailJson = objectMapper.writeValueAsString(requestEntity);
//    } catch (JsonProcessingException e) {
//      e.printStackTrace();
//    }
//
//    return sendPostRequest(url, key, emailJson);
//  }

//  private HttpResponse sendPostRequest(String url, String service, String requestEntity) throws
// FXDefaultException {
//    HttpClient client = HttpClientBuilder.create().build();
//    HttpResponse response = null;
//
//    HttpPost httpReq = new HttpPost(url);
//    httpReq.setHeader("Content-type", "application/json");
//    HttpEntity stringEntity = new StringEntity(requestEntity, ContentType.APPLICATION_JSON);
//
//    httpReq.setEntity(stringEntity);
//
//    try {
//      response = client.execute(httpReq);
//      int respCode = response.getStatusLine().getStatusCode();
//
//      if (!(respCode >= 200 && respCode < 300)) {
//        logger.error("REST API request failed. Error code " + respCode);
//        throw new FXDefaultException("-1", "API_CONNECTION_FAILED",
//                String.format(template1, service, respCode),
//                new Date(), HttpStatus.INTERNAL_SERVER_ERROR);
//      }
//      logger.debug(String.format(template3, service));
//    } catch (IOException e) {
//      throw new FXDefaultException("",
//              String.format(template2, service),
//              e.getMessage(), new Date(), HttpStatus.BAD_REQUEST);
//    }
//    return response;
//  }


  private HttpResponse sendGetRequest(String url, String service,
                                      HashMap<String, String> headers) throws FXDefaultException {

    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = null;

    HttpGet httpReq = new HttpGet(url);
    httpReq.setHeader("Content-type", "application/json");

    //Set headers
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        httpReq.setHeader(entry.getKey(), entry.getValue());
      }
    }

    try {
      response = client.execute(httpReq);
      int respCode = response.getStatusLine().getStatusCode();

      if (!(respCode >= 200 && respCode < 300)) {
        logger.error("REST API request failed. Error code " + respCode);
        throw new FXDefaultException("-1", "API_CONNECTION_FAILED",
                String.format(template1, service, respCode),
                new Date(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      logger.debug(String.format(template3, service));
    } catch (IOException e) {
      throw new FXDefaultException("",
              String.format(template2, service),
              e.getMessage(), new Date(), HttpStatus.BAD_REQUEST);
    }
    return response;
  }


  @Override
  public RestResponsePage sendServiceRequest(String serviceUrl,
                                             HashMap<String, String> parameters, HashMap<String,
          String> headers, String service) throws
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
      logger.debug("Service response : " + httpResponse.getStatusLine().getStatusCode());
      throw new FXDefaultException("-1", "FAILED_REQUEST", "Service Request Failed to " + service
              , new Date(), HttpStatus.BAD_REQUEST);
    }
  }

  private HttpResponse sendPostRequest(String url, String service,
                                       HashMap<String, String> headers, Object object) throws FXDefaultException {

    ObjectMapper objectMapper = new ObjectMapper();
    String jsonString = null;
    //Convert object to jsonstring to be set for POST Req
    try {
      jsonString = objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      logger.error("Object to JSON conversion error in POST req to service : " + service);
    }
    //Convert json String to HTTPEntity
    HttpEntity stringEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);

    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = null;

    HttpPost httpReq = new HttpPost(url);
    httpReq.setEntity(stringEntity);
    httpReq.setHeader("Content-type", "application/json");

    //Set headers
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        httpReq.setHeader(entry.getKey(), entry.getValue());
      }
    }

    try {
      response = client.execute(httpReq);
      int respCode = response.getStatusLine().getStatusCode();

      if (!(respCode >= 200 && respCode < 300)) {
        logger.error("REST API request failed. Error code " + respCode);
        throw new FXDefaultException("-1", "API_CONNECTION_FAILED",
                String.format(template1, service, respCode),
                new Date(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      logger.debug(String.format(template3, service));
    } catch (IOException e) {
      throw new FXDefaultException("",
              String.format(template2, service),
              e.getMessage(), new Date(), HttpStatus.BAD_REQUEST);
    }
    return response;
  }

  private String buildGetURL(String baseURL, Map<String, String> queryParam, String service) {
    URIBuilder builder;
    try {
      builder = new URIBuilder(baseURL);
      queryParam.entrySet().forEach(x -> builder.addParameter(x.getKey(), x.getValue()));
      return builder.build().toString();
    } catch (URISyntaxException e) {
      logger.error("Query parameter map processing error. Service : " + service);
      e.printStackTrace();
    }
    return null;
  }
}
