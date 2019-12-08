package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.fx.aml.ChannelRisk;
import com.loits.fx.aml.Module;

public interface AMLChannelRiskService {

  ChannelRisk calculateChannelRisk(Long customerId, Module module, String user,
                                          String tenent) throws FXDefaultException;
}
