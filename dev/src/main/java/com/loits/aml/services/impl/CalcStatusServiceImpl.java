package com.loits.aml.services.impl;


import com.google.gson.Gson;
import com.loits.aml.commons.CalcStatusCodes;
import com.loits.aml.domain.CalcStatus;
import com.loits.aml.repo.CalcStatusRepository;
import com.loits.aml.services.CalcStatusService;
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
  CalcStatusRepository syncStatusRepository;

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
    if (currentStatus.equalsIgnoreCase(CalcStatusCodes.CALC_COMPLETED)) {
      calcStatus.setEDate(new Timestamp(new Date().getTime()));
    } else calcStatus.setMDate(new Timestamp(new Date().getTime()));
    calcStatus.setCronStatus(currentStatus);

    return this.syncStatusRepository.save(calcStatus);
  }

  @Override
  public CalcStatus saveSyncStatus(String currentStatus, String type, int page, int size) {
    CalcStatus newSync = new CalcStatus();
    newSync.setType(type);
    newSync.setSDate(new Timestamp(new Date().getTime()));
    newSync.setMDate(new Timestamp(new Date().getTime()));
    newSync.setCronStatus(currentStatus);
    return this.syncStatusRepository.save(newSync);
  }
}
