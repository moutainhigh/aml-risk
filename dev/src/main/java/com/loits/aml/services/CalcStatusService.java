package com.loits.aml.services;

import com.loits.aml.domain.CalcStatus;

import java.util.HashMap;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

public interface CalcStatusService {

  CalcStatus saveCalcStatus(String tenent, CalcStatus calcStatus, String jobId,
                            String currentStatus, String type, HashMap<String, Object> meta);

  CalcStatus saveSyncStatus(String currentStatus,
                            String type, int page, int size);

}
