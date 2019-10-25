package com.loits.aml.services;


import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.ServiceMetadata;

/**
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */

public interface ServiceMetadataService {

  ServiceMetadata getServiceMetadata(String key) throws FXDefaultException;

}
