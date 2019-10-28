package com.loits.aml.services;

import com.loits.aml.domain.SyncStatus;

import java.util.Date;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

public interface SyncStatusService {

  SyncStatus updateSyncStatus(String module, SyncStatus lastSyncStatus,
                              String currentStatus,
                              String type);

  Date getLastSuccessfulAMLDataSyncDate(String type, String module, SyncStatus thisSync);


  SyncStatus getSyncStartDayPopulated(String KEY, String type);
}
