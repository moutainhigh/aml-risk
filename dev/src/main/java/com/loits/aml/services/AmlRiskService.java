package com.loits.aml.services;

import com.loits.aml.config.RestResponsePage;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.fx.aml.OverallRisk;

import java.io.IOException;

import java.util.HashMap;

public interface AmlRiskService {
    Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent) throws FXDefaultException, IOException, ClassNotFoundException;

    Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user, String tenent) throws FXDefaultException;

    //temporary for testing
    OverallRisk runRiskCronJob(String user, String tenent, Long id) throws FXDefaultException;

    OverallRisk runRiskCronJob2(String user, String tenent, int page, int size) throws FXDefaultException;

     public RestResponsePage sendServiceRequest(String
                                                     serviceUrl,
                                             HashMap<String, String> parameters, HashMap<String,
          String> headers, String service) throws FXDefaultException;
}
