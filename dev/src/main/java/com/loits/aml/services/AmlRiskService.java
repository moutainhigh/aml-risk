package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;

public interface AmlRiskService {
    Object calcOnboardingRisk(OnboardingCustomer customer, String user) throws FXDefaultException;

    Object calcRisk(String customerCode, String module, String otherIdentity, String user) throws FXDefaultException;
}
