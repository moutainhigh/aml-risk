package com.loits.aml.controller;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.services.RiskService;
import com.redhat.aml.Customer;
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
    @PostMapping(path= "/{tenent}", produces = "application/json")
    public ResponseEntity<?> calculateRisk(@PathVariable(value = "tenent") String tenent,
//                                           @RequestParam(value = "projection") String projection,
                                           @RequestBody @Valid Customer customer,
                                           @RequestHeader("user") String user,
                                           @RequestParam("timestamp") Timestamp timestamp
    ) throws FXDefaultException {
        Resource resource = new Resource(riskService.calcRisk(customer, user, timestamp));
        return ResponseEntity.ok(resource);
    }

}
