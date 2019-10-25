package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.fx.aml.OverallRisk;

public interface KieService {

    OverallRisk getOverallRisk(OverallRisk overallRisk) throws FXDefaultException;

}
