package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.fx.aml.OverallRisk;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface RiskService {

  CompletableFuture<?> calculateRiskForCustomerBase(String user, String tenent,Integer pageLimit,Integer recordLimit);

  Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent)
          throws FXDefaultException, IOException, ClassNotFoundException;


}
