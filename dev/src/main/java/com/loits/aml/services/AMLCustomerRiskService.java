package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.Customer;
import com.loits.fx.aml.CustomerRisk;
import com.loits.fx.aml.Module;
import com.loits.fx.aml.ProductRisk;

public interface AMLCustomerRiskService {

  CustomerRisk calculateCustomerRisk(Customer customer, Module ruleModule, String user,
                                            String tenent) throws FXDefaultException;

}
