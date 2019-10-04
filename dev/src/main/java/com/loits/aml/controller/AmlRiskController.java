package com.loits.aml.controller;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.services.AmlRiskService;
import com.redhat.aml.RiskCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
public class AmlRiskController {

    @Autowired
    AmlRiskService amlRiskService;

    /**
     * Overall Risk Calculation
     * @param tenent
     * @param user
     * @return
     * @throws FXDefaultException
     */
    @GetMapping(path= "/{tenent}", produces = "application/json")
    public ResponseEntity<?> calculateRisk(@PathVariable(value = "tenent") String tenent,
                                           @RequestParam(value = "projection") String projection,
                                           @RequestParam(value= "customer_code", required = true) String customerCode,
                                           @RequestParam(value= "module" , required = true) String module,
                                           @RequestParam(value= "other_identity") String otherIdentity,
                                           @RequestHeader("user") String user
    ) throws FXDefaultException {
        Resource resource = new Resource(amlRiskService.calcRisk(customerCode, module, otherIdentity, user));
        return ResponseEntity.ok(resource);
    }


    @PostMapping(path= "/{tenent}", produces = "application/json")
    public ResponseEntity<?> calculateRiskOnOnboarding(@PathVariable(value = "tenent") String tenent,
                                                       @RequestParam(value = "projection") String projection,
                                                       @RequestBody @Valid RiskCustomer customer,
                                                       @RequestHeader("user") String user
    ) throws FXDefaultException {
        Resource resource = new Resource(amlRiskService.calcOnboardingRisk(customer, user));
        return ResponseEntity.ok(resource);
    }
}
