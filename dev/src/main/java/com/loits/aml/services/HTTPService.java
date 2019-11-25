package com.loits.aml.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loits.aml.core.FXDefaultException;
import org.apache.commons.collections.map.HashedMap;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

public interface HTTPService {

  public <T, classType, binderType> List<classType> getDataFromPage(String key, String url, Map<String,
          String> queryParam, TypeReference typeReference) throws FXDefaultException,
          IOException, ClassNotFoundException;

  public <T, classType, binderType> List<classType> getDataFromList(String key, String url, Map<String,
          String> queryParam, TypeReference typeReference) throws FXDefaultException,
          IOException, ClassNotFoundException;

//  public <T, classType, binderType> List<classType> sendData(String key, String url, Map<String,
//          String> queryParam, HashMap<String, String> headers, Class classType) throws FXDefaultException,
//          IOException, ClassNotFoundException;

  public <T, classType, binderType> T sendData(String key, String url, Map<String, String> queryParam, HashMap<String, String> headers, Class classType, Object object) throws FXDefaultException, IOException, ClassNotFoundException;

//  HttpResponse sendEmail(String key, String url, Map<String,
//          String> queryParam, Object requestEntity) throws FXDefaultException,
//          IOException, ClassNotFoundException;

}
