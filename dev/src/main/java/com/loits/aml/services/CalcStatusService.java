package com.loits.aml.services;

import com.loits.aml.domain.CalcLog;
import com.loits.aml.domain.CalcStatus;
import com.loits.aml.domain.CalcTasks;

import java.util.HashMap;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

public interface CalcStatusService {

  CalcStatus getLastCalculation(String type);

  CalcStatus saveCalcStatus(String tenent, CalcStatus calcStatus, String jobId,
                            String currentStatus, String type, HashMap<String, Object> meta);

  CalcTasks saveCalcTask(CalcTasks calcTask, Long calStatusId, String jobId,
                         String currentStatus, HashMap<String, Object> meta);

  CalcLog saveCalcLog(CalcTasks calcTask, String reference, String error, String refKey,
                      String refValue,
                      String refTalbe, Exception e);

  CalcStatus saveSyncStatus(String currentStatus,
                            String type, int page, int size);

}
