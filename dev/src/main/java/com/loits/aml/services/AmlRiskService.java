package com.loits.aml.services;

import com.loits.aml.config.RestResponsePage;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.fx.aml.OverallRisk;

import java.io.IOException;

import java.util.HashMap;

public interface AmlRiskService {
<<<<<<< HEAD
    Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent) throws FXDefaultException, IOException, ClassNotFoundException;

    Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user, String tenent) throws FXDefaultException;

    OverallRisk runRiskCronJob(String user, String tenent) throws FXDefaultException;
=======
  Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent) throws FXDefaultException;

  Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user,
                         String tenent) throws FXDefaultException;

  public RestResponsePage sendServiceRequest(String
                                                     serviceUrl,
                                             HashMap<String, String> parameters, HashMap<String,
          String> headers, String service) throws FXDefaultException;
>>>>>>> feature/bulk-risk-calculation
}
