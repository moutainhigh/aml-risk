package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;

public interface AmlRiskService {
    Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent) throws FXDefaultException;

    Object getCustomerRisk(String customerCode, String module, String otherIdentity, String user, String tenent) throws FXDefaultException;
}
