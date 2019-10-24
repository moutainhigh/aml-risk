package com.loits.aml.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loits.aml.core.FXDefaultException;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

public interface HTTPService {

  public <T, classType, binderType> List<classType> getData(String key, String url, Map<String,
          String> queryParam, String method, TypeReference typeReference) throws FXDefaultException,
          IOException, ClassNotFoundException;

  HttpResponse sendEmail(String key, String url, Map<String,
          String> queryParam, String method, Object requestEntity) throws FXDefaultException,
          IOException, ClassNotFoundException;

}
