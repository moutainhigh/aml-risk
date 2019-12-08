package com.loits.aml.services.impl;


import com.google.gson.Gson;
import com.loits.aml.commons.CalcStatusCodes;
import com.loits.aml.domain.CalcLog;
import com.loits.aml.domain.CalcStatus;
import com.loits.aml.domain.CalcTasks;
import com.loits.aml.repo.CalcLogRepository;
import com.loits.aml.repo.CalcStatusRepository;
import com.loits.aml.repo.CalcTasksRepository;
import com.loits.aml.services.CalcStatusService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

@Service
public class CalcStatusServiceImpl implements CalcStatusService {

  Logger logger = LogManager.getLogger(CalcStatusServiceImpl.class);

  @Autowired
  CalcStatusRepository calcStatusRepository;

  @Autowired
  CalcTasksRepository calcTasksRepository;

  @Autowired
  CalcLogRepository calcLogRepository;

  @Value("${global.date.format}")
  private String dateFormat;

  SimpleDateFormat sdf;

  @PostConstruct
  public void init() {
    this.sdf = new SimpleDateFormat(dateFormat);
  }

  @Override
  public CalcStatus saveCalcStatus(String tenent, CalcStatus calcStatus, String jobId,
                                   String currentStatus,
                                   String type, HashMap<String, Object> meta) {

    Gson gson = new Gson();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());

    if (currentStatus.equalsIgnoreCase(CalcStatusCodes.CALC_INITIATED)) {
      // set default values
      calcStatus.setSDate(cal.getTime());
      calcStatus.setCronStatus(CalcStatusCodes.CALC_INITIATED);
      calcStatus.setType(type);
      calcStatus.setErrorCount(0);
      calcStatus.setUpdatedCount(0);
      calcStatus.setTotalRecords(0);
      calcStatus.setJobId(jobId);
      calcStatus.setModule(tenent);
    }

    // see if HM contain any high level meta
    if (meta != null && !meta.isEmpty()) {
      for (Map.Entry<String, Object> entry : meta.entrySet()) {
        if (entry.getKey().equalsIgnoreCase("fetched")) {
          calcStatus.setTotalRecords((Integer) entry.getValue());
        }
      }
      calcStatus.setMeta(gson.toJson(meta));
    }

    // set appropriate date
    if (currentStatus.equalsIgnoreCase(CalcStatusCodes.CALC_COMPLETED)
            || currentStatus.equalsIgnoreCase(CalcStatusCodes.CALC_ERROR)) {
      calcStatus.setEDate(new Timestamp(new Date().getTime()));
    } else calcStatus.setMDate(new Timestamp(new Date().getTime()));
    calcStatus.setCronStatus(currentStatus);

    return this.calcStatusRepository.save(calcStatus);
  }

  @Override
  public CalcTasks saveCalcTask(CalcTasks calcTask, Long calStatusId, String jobId,
                                String currentStatus,
                                HashMap<String, Object> meta) {
    Gson gson = new Gson();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    CalcStatus calcStatus = this.calcStatusRepository.findById(calStatusId).get();

    if (currentStatus.equalsIgnoreCase(CalcStatusCodes.CALC_INITIATED)) {
      // set default values
      calcTask.setSDate(cal.getTime());
      calcTask.setCronStatus(CalcStatusCodes.CALC_INITIATED);
      calcTask.setErrorCount(0);
      calcTask.setUpdatedCount(0);
      calcTask.setTotalRecords(0);
      calcTask.setJobId(jobId);
    }

    // see if HM contain any high level meta
    if (meta != null && !meta.isEmpty()) {
      for (Map.Entry<String, Object> entry : meta.entrySet()) {
        if (entry.getKey().equalsIgnoreCase("fetched")) {
          calcTask.setTotalRecords((Integer) entry.getValue());
        } else if (entry.getKey().equalsIgnoreCase("processed")) {
          calcTask.setProcessedCount((Integer) entry.getValue());
        } else if (entry.getKey().equalsIgnoreCase("updated")) {
          calcTask.setUpdatedCount((Integer) entry.getValue());
        } else if (entry.getKey().equalsIgnoreCase("errorCount")) {
          calcTask.setErrorCount((Integer) entry.getValue());
        }
      }
      calcTask.setMeta(gson.toJson(meta));
    }

    // set appropriate date
    if (currentStatus.equalsIgnoreCase(CalcStatusCodes.CALC_COMPLETED)) {
      calcTask.setEDate(new Timestamp(new Date().getTime()));
    } else calcTask.setMDate(new Timestamp(new Date().getTime()));
    calcTask.setCronStatus(currentStatus);

    calcTask.setCalcStatus(calcStatus);

    return this.calcTasksRepository.save(calcTask);
  }

  @Override
  public CalcLog saveCalcLog(CalcTasks calcTask, String reference, String error, String refKey,
                             String refValue,
                             String refTalbe, Exception e) {
    CalcLog log = new CalcLog();

    if (e != null) {
      log.setDescription(e.getMessage());
      log.setStacktrace(ExceptionUtils.getStackTrace(e));
    }
    log.setReference(reference);
    log.setRefTable(refTalbe);
    log.setRefKey(refKey);
    log.setRefValue(refValue);
    log.setDate(new Date());
    log.setRaiseNotifications("Y");

    if (calcTask != null) {
      log.setCalcTask(calcTask);
    }

    return this.calcLogRepository.save(log);
  }

  @Override
  public CalcStatus saveSyncStatus(String currentStatus, String type, int page, int size) {
    CalcStatus newSync = new CalcStatus();
    newSync.setType(type);
    newSync.setSDate(new Timestamp(new Date().getTime()));
    newSync.setMDate(new Timestamp(new Date().getTime()));
    newSync.setCronStatus(currentStatus);
    return this.calcStatusRepository.save(newSync);
  }
}
