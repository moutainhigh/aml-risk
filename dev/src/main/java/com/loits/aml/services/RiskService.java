package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;

import java.util.concurrent.CompletableFuture;

public interface RiskService {

  CompletableFuture<?> calculateRiskForCustomerBase(String user, String tenent);
}
