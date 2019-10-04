package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.redhat.aml.OnboardingCustomer;

import java.sql.Timestamp;

public interface AmlRiskService {
    Object calcRisk(String nic, String user, Timestamp timestamp) throws FXDefaultException;
    Object calcOnboardingRisk(OnboardingCustomer customer, String user, Timestamp timestamp) throws FXDefaultException;
}
