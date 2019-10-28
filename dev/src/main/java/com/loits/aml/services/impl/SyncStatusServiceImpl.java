package com.loits.aml.services.impl;


import com.loits.aml.commons.SyncStatusCodes;
import com.loits.aml.domain.SyncStatus;
import com.loits.aml.repo.SyncStatusRepository;
import com.loits.aml.services.SyncStatusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
  public SyncStatus updateSyncStatus(String module, SyncStatus lastSyncStatus,
                                     String currentStatus, String type) {

    lastSyncStatus.setModule(module);
    lastSyncStatus.setType(type);
    lastSyncStatus.setMDate(new Timestamp(new Date().getTime()));
    lastSyncStatus.setCronStatus(currentStatus);

    return this.syncStatusRepository.save(lastSyncStatus);
  }


  @Override
  public Date getLastSuccessfulAMLDataSyncDate(String type, String module, SyncStatus thisSync) {

    Sort sort = Sort.by("sDate").descending(); // data sorting strategy
    SyncStatus lastSync = this.syncStatusRepository.findFirstByType(
            type, sort);

    if (lastSync.getCronStatus().equals(SyncStatusCodes.SYNC_COMPLETED)) {
      // last sync was successful. Just sync the curren data
      logger.debug("Last sync has completed successfully. Current sync date is valid");
      return thisSync.getSDate();
    } else if (lastSync.getCronStatus().equals(SyncStatusCodes.SYNC_ERROR)) {
      // last sync had an error, include last sync in this sync.
      logger.debug("Last sync has failed. Sync start date set to last sync start date");
      return lastSync.getSDate();
    } else return null;
  }


  @Override
  public SyncStatus getSyncStartDayPopulated(String KEY, String type) {

    Sort sort = Sort.by("id").descending(); // data sorting strategy
    SyncStatus lastSync = this.syncStatusRepository.findFirstByType(type, sort);

    Calendar nextSyncStart = Calendar.getInstance();
    SyncStatus nextSync = new SyncStatus();
    nextSync.setEDate(new Timestamp(new Date().getTime()));

    if (lastSync == null) {
      // first sync
      logger.debug(KEY + "synchronizing data for the first time");
      nextSyncStart.setTime(new Date());
      nextSyncStart.add(Calendar.DATE, -1);
    } else if (lastSync.getCronStatus().equals(SyncStatusCodes.SYNC_COMPLETED)) {
      logger.debug("last sync has been successful. Run a new sync");
      // Previous sync strategy has fetched data or pushed to AML
      nextSyncStart.setTime(lastSync.getEDate());
    } else {
      // Last sync had an error. Start over.
      logger.debug(KEY + "last sync had an error. Start over");
      nextSyncStart.setTime(lastSync.getSDate());
    }

    nextSync.setSDate(new Timestamp(nextSyncStart.getTime().getTime()));
    nextSync.setMDate(nextSync.getSDate()); // modified date is same as start date at this point

    logger.debug(KEY + String.format("Sync task run from : %s   to : %s",
            sdf.format(nextSync.getSDate().getTime()),
            sdf.format(nextSync.getEDate().getTime())));

    return nextSync;
  }
}
