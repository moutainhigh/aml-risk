package com.loits.aml.services;

import com.loits.aml.core.FXDefaultException;
import com.redhat.aml.OverallRisk;

public interface KieService {

    OverallRisk getOverallRisk(OverallRisk overallRisk) throws FXDefaultException;

}
