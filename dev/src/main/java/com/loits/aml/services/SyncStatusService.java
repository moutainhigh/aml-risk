package com.loits.aml.services;

import com.loits.aml.domain.SyncStatus;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

public interface SyncStatusService {

  SyncStatus updateSyncStatus(SyncStatus lastSyncStatus,
                              String currentStatus );

  SyncStatus saveSyncStatus(String currentStatus,
                            String type, int page, int size);

}
