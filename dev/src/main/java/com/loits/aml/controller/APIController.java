package com.loits.aml.controller;

import com.loits.aml.commons.HealthCheck;
import com.loits.aml.core.FXDefaultException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Handlign API status
 *
 * @author Lahiru Bandara - Infinitum360
 * @version 1.0.0
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1")
@SuppressWarnings("unchecked")
public class APIController {

  Logger logger = LogManager.getLogger(APIController.class);

  @Autowired
  HealthCheck healthCheck;

  /**
   * Health check endpoint
   *
   * @return
   */
  @GetMapping(path = "/{tenent}", produces = "application/json")
  public @ResponseBody
  ResponseEntity<?> getChannels(@PathVariable String tenent) throws FXDefaultException {

    logger.debug(String.format("Requested tenent : %s", tenent));
    return ResponseEntity.ok(new Resource<>(healthCheck.getStatus()));
  }
}