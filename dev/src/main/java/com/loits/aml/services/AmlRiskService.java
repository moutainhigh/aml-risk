package com.loits.aml.services;

import com.loits.aml.config.RestResponsePage;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.Customer;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.fx.aml.OverallRisk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

import java.util.Date;
import java.util.HashMap;

public interface AmlRiskService {
    Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent) throws FXDefaultException, IOException, ClassNotFoundException;

    Page<?> getAvailableCustomerRisk(String customerCode, Pageable pageable, String module, String otherIdentity, Date from, Date to, String user, String tenent) throws FXDefaultException;

    //temporary for testing
    OverallRisk calculateRiskByCustomer(String user, String tenent, Long id) throws FXDefaultException;

    void runRiskCronJob(Boolean calculateCustRisk, String user, String tenent, Customer customer) throws FXDefaultException;

     public RestResponsePage sendServiceRequest(String
                                                     serviceUrl,
                                             HashMap<String, String> parameters, HashMap<String,
          String> headers, String service) throws FXDefaultException;
}
