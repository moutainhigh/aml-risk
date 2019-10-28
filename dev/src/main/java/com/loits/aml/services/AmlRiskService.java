package com.loits.aml.services;

import com.loits.aml.config.RestResponsePage;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;

import java.util.HashMap;

public interface AmlRiskService {
  Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent) throws FXDefaultException;

  Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user,
                         String tenent) throws FXDefaultException;

  public RestResponsePage sendServiceRequest(String
                                                     serviceUrl,
                                             HashMap<String, String> parameters, HashMap<String,
          String> headers, String service) throws FXDefaultException;
}
