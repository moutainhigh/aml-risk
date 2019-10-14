package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.redhat.aml.RiskCustomer;

public interface AmlRiskService {
    Object calcOnboardingRisk(RiskCustomer customer, String user) throws FXDefaultException;

    Object calcRisk(String customerCode, String module, String otherIdentity, String user) throws FXDefaultException;
}
