package com.loits.aml.services.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

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
  public <T, classType, binderType> List<classType> getData(String key, String url,
                                                                  Map<String, String> queryParam,
                                                                  String method,
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
    httpResponse = sendGetRequest(url, key);
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
  public HttpResponse sendEmail(String key, String url, Map<String, String> queryParam, String method, Object requestEntity) throws FXDefaultException, IOException, ClassNotFoundException {
    ObjectMapper objectMapper = new ObjectMapper();
    String emailJson = null;
    try {
      emailJson = objectMapper.writeValueAsString(requestEntity);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return sendPostRequest(url, key, emailJson);
  }

  private HttpResponse sendPostRequest(String url, String service, String requestEntity) throws FXDefaultException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = null;

    HttpPost httpReq = new HttpPost(url);
    httpReq.setHeader("Content-type", "application/json");
    HttpEntity stringEntity = new StringEntity(requestEntity, ContentType.APPLICATION_JSON);

    httpReq.setEntity(stringEntity);

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


  private HttpResponse sendGetRequest(String url, String service) throws FXDefaultException {

    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = null;

    HttpGet httpReq = new HttpGet(url);
    httpReq.setHeader("Content-type", "application/json");

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
