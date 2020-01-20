package com.loits.aml.services;

import com.loits.aml.commons.RiskCalcParams;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.Customer;
import com.loits.fx.aml.OverallRisk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface AMLRiskService {

  Page<?> getAvailableCustomerRisk(String customerCode, Pageable pageable, String module,
                                   String otherIdentity, Date from, Date to, String user,
                                   String tenent) throws FXDefaultException;

  //temporary for testing
  OverallRisk calculateRiskByCustomer(String user, String tenent, Long id)
          throws FXDefaultException;

  boolean runRiskCronJob(RiskCalcParams riskCalcParams,String user,
                         String tenent,
                         Customer customer) throws FXDefaultException;
}
