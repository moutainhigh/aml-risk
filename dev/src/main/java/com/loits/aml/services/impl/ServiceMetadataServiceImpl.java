package com.loits.aml.services.impl;


import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.ServiceMetadata;
import com.loits.aml.repo.ServiceMetadataRepository;
import com.loits.aml.services.ServiceMetadataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

@Service
public class ServiceMetadataServiceImpl implements ServiceMetadataService {

  Logger logger = LogManager.getLogger(ServiceMetadataServiceImpl.class);

  @Autowired
  ServiceMetadataRepository serviceMetadataRepository;

  @Override
  public ServiceMetadata getServiceMetadata(String key) throws FXDefaultException {
    if (!this.serviceMetadataRepository.existsById(key)) {
      // key not found
      logger.error("Meta key not found");
      throw new FXDefaultException("-1", "DATA_NOT_FOUND", "Requested meta key not found",
              new Date());
    }
    return this.serviceMetadataRepository.findOneByMetaKey(key);
  }
}
