package com.loits.aml.services;

import com.loits.aml.commons.RiskCalcParams;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.fx.aml.OverallRisk;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RiskService {

  @Async
  CompletableFuture<?> calculateRiskForCustomerBase(String projection, String user, String tenent, RiskCalcParams riskCalcParams);

  Object calcOnboardingRisk(OnboardingCustomer customer, String user, String tenent)
          throws FXDefaultException, IOException, ClassNotFoundException, URISyntaxException, InvocationTargetException, IllegalAccessException;

  List<OverallRisk> calculateForModuleCustomers(String user, String tenent, List<OverallRisk> customers)
          throws FXDefaultException, ExecutionException, InterruptedException;
}
