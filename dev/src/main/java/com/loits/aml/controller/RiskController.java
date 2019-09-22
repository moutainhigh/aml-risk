package com.loits.aml.controller;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.services.RiskService;
import com.redhat.aml.Customer;
import com.redhat.aml.OverallRisk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;

/**
 * Managing Riskrelated operations
 *
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1")
@SuppressWarnings("unchecked")
public class RiskController {

    @Autowired
    RiskService riskService;

    /**
     * Overall Risk Calculation
     * @param tenent
     * @param projection
     * @param customer
     * @param user
     * @param timestamp
     * @return
     * @throws FXDefaultException
     */
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> calculateRisk(@RequestParam(value = "tenent", defaultValue = "1") String tenent,
                                           @RequestParam(value = "projection") String projection,
                                           @RequestBody @Valid Customer customer,
                                           @RequestHeader("user") String user,
                                           @RequestParam("timestamp") Timestamp timestamp
    ) throws FXDefaultException {
        Resource resource = new Resource(riskService.calcRisk(projection, customer, user, timestamp));
        return ResponseEntity.ok(resource);
    }

}
