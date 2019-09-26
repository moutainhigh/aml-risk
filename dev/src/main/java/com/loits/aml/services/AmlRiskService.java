package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.redhat.aml.Customer;
import com.redhat.aml.OnboardingCustomer;
import com.redhat.aml.OverallRisk;

import java.sql.Timestamp;

public interface AmlRiskService {
    Object calcRisk(Customer customer, String user, Timestamp timestamp) throws FXDefaultException;
    Object calcOnboardingRisk(OnboardingCustomer customer, String user, Timestamp timestamp) throws FXDefaultException;
}
