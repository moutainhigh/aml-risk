package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.fx.aml.OverallRisk;

public interface AmlRiskService {
    Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent) throws FXDefaultException;

    Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user, String tenent) throws FXDefaultException;

    OverallRisk runRiskCronJob(String user, String tenent) throws FXDefaultException;
}
