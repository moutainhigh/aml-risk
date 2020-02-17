package com.loits.aml.services;

import com.loits.aml.commons.RiskCalcParams;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.AmlRisk;
import com.loits.aml.dto.Customer;
import com.loits.fx.aml.OverallRisk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public interface AMLRiskService {

  Page<?> getAvailableCustomerRisk(String customerCode, Pageable pageable, String module,
                                   String otherIdentity, Date from, Date to, String user,
                                   String tenent) throws FXDefaultException;

  //temporary for testing
  OverallRisk calculateRiskByCustomer(String user, String tenent, Long id, String projection)
          throws FXDefaultException;

  AmlRisk runRiskCronJob(RiskCalcParams riskCalcParams, String user,
                         String tenent,
                         Customer customer) throws FXDefaultException;

  void saveRiskCalculationTime(Long customerId, Timestamp riskCalcOn,
                               String tenent);

  CompletableFuture<OverallRisk> calcRiskForCustomer(Long customerId, String user, String tenent, String projection);

}
