package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.Transaction;
import com.loits.fx.aml.Module;
import com.loits.fx.aml.ProductRisk;

import java.util.List;

public interface AMLProductRiskService {

  ProductRisk calculateProductRisk(Long customerId, Module ruleModule, String user,
                                   String tenent, List<Transaction> transactionList) throws FXDefaultException;

}
