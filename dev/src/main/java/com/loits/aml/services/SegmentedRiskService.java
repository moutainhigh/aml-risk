package com.loits.aml.services;

import com.loits.aml.commons.RiskCalcParams;
import com.loits.aml.core.FXDefaultException;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface SegmentedRiskService {

  @Async
  public CompletableFuture<?> calculateCustomerSegmentRisk(RiskCalcParams riskCalcParams,
                                                           Long calId, String user, String tenent,
                                                           int page,
                                                           int size);

  Object calculateRiskForBatch(String user, String tenent, RiskCalcParams riskCalcParams) throws FXDefaultException;
}
