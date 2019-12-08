package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.fx.aml.Module;
import com.loits.fx.aml.ProductRisk;

public interface AMLProductRiskService {

  ProductRisk calculateProductRisk(Long customerId, Module ruleModule, String user,
                                   String tenent) throws FXDefaultException;

}
