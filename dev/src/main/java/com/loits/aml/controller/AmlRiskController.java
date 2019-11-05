package com.loits.aml.controller;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.aml.services.AmlRiskService;
import com.loits.aml.services.RiskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

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

    Logger logger = LogManager.getLogger(AmlRiskController.class);

    @Autowired
    AmlRiskService amlRiskService;

    @Autowired
    RiskService riskService;

    /**
     * Overall Risk Calculation
     * @param tenent
     * @param user
     * @return
     * @throws FXDefaultException
     */
    @GetMapping(path= "/{tenent}", produces = "application/json")
    public ResponseEntity<?> calculateRisk(@PathVariable(value = "tenent") String tenent,
                                           @RequestParam(value= "customer_code", required = true) String customerCode,
                                           @RequestParam(value= "module" , required = true) String module,
                                           @RequestParam(value= "other_identity") String otherIdentity,
                                           @RequestHeader(value = "user", defaultValue = "sysUser") String user
    ) throws FXDefaultException {
        Resource resource = new Resource(amlRiskService.getCustomerRisk(customerCode, module, otherIdentity, user, tenent));
        return ResponseEntity.ok(resource);
    }


    @PostMapping(path= "/{tenent}", produces = "application/json")
    public ResponseEntity<?> calculateRiskOnOnboarding(@PathVariable(value = "tenent") String tenent,
                                                       @RequestBody @Valid OnboardingCustomer customer,
                                                       @RequestHeader(value = "user", defaultValue = "sysUser") String user
    ) throws FXDefaultException, IOException, ClassNotFoundException {
        Resource resource = new Resource(amlRiskService.calcOnboardingRisk(customer, user, tenent));
        return ResponseEntity.ok(resource);
    }

    @GetMapping(path= "/{tenent}/calculate-one/{id}", produces = "application/json")
    public ResponseEntity<?> calculateAllRisk(@PathVariable(value = "tenent") String tenent,
                                              @PathVariable(value = "id") Long id,
                                           @RequestHeader(value = "user", defaultValue = "sysUser") String user
    ) throws FXDefaultException {
        Resource resource = new Resource(amlRiskService.calculateRiskByCustomer(user, tenent, id));
        return ResponseEntity.ok(resource);
    }

  @PostMapping(path = "/{tenent}/calculate", produces = "application/json")
  public ResponseEntity<?> calculateRiskOnOnboarding(@PathVariable(value = "tenent") String tenent,
                                                     @RequestHeader(value = "user", defaultValue
                                                             = "sysUser") String user
  ) throws FXDefaultException {

    logger.debug(String.format("Starting to calculate risk for the customer base. " +
            "User : %s , Tenent : %s", user, tenent));
    Resource resource = new Resource(riskService.calculateRiskForCustomerBase(user, tenent));
    return ResponseEntity.ok(resource);
  }
}
