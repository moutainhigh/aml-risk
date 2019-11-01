package com.loits.aml.services.impl;


import com.loits.aml.commons.SyncStatusCodes;
import com.loits.aml.domain.SyncStatus;
import com.loits.aml.repo.SyncStatusRepository;
import com.loits.aml.services.SyncStatusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

@Service
public class SyncStatusServiceImpl implements SyncStatusService {

  Logger logger = LogManager.getLogger(SyncStatusServiceImpl.class);

  @Autowired
  SyncStatusRepository syncStatusRepository;

  @Value("${global.date.format}")
  private String dateFormat;

  SimpleDateFormat sdf;

  @PostConstruct
  public void init() {
    this.sdf = new SimpleDateFormat(dateFormat);
  }

  @Override
  public SyncStatus updateSyncStatus(SyncStatus lastSyncStatus, String currentStatus) {

    if (currentStatus.equalsIgnoreCase(SyncStatusCodes.SYNC_COMPLETED)) {
      lastSyncStatus.setEDate(new Timestamp(new Date().getTime()));
    } else lastSyncStatus.setMDate(new Timestamp(new Date().getTime()));
    lastSyncStatus.setCronStatus(currentStatus);

    return this.syncStatusRepository.save(lastSyncStatus);
  }

  @Override
  public SyncStatus saveSyncStatus(String currentStatus, String type, int page, int size) {
    SyncStatus newSync = new SyncStatus();
    newSync.setType(type);
    newSync.setSDate(new Timestamp(new Date().getTime()));
    newSync.setMDate(new Timestamp(new Date().getTime()));
    newSync.setCronStatus(currentStatus);
    newSync.setPage(page);
    newSync.setSize(size);
    return this.syncStatusRepository.save(newSync);
  }
}
