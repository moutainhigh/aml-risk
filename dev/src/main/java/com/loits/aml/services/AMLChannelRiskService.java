package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.Transaction;
import com.loits.fx.aml.ChannelRisk;
import com.loits.fx.aml.Module;

import java.util.List;

public interface AMLChannelRiskService {

  ChannelRisk calculateChannelRisk(Long customerId, Module module, String user,
                                          String tenent, List<Transaction> transactionList) throws FXDefaultException;
}
