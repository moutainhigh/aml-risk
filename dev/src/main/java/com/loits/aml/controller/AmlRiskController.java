package com.loits.aml.controller;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.aml.services.AMLRiskService;
import com.loits.aml.services.RiskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;

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
  AMLRiskService amlRiskService;

  @Autowired
  RiskService riskService;

  /**
   * Overall Risk Calculation
   *
   * @param tenent
   * @param user
   * @return
   * @throws FXDefaultException
   */
  @GetMapping(path = "/{tenent}", produces = "application/json")
  public @ResponseBody
  Page<?> getAvailableRisk(@PathVariable(value = "tenent") String tenent,
                           @PageableDefault(size = 10) Pageable pageable,
                           @RequestParam(value = "customer_code", required = false) String customerCode,
                           @RequestParam(value = "module", required = true) String module,
                           @RequestParam(value = "other_identity", required = false) String otherIdentity,
                           @RequestParam(value = "fromDate", required = false) Date from,
                           @RequestParam(value = "toDate", required = false) Date to,
                           @RequestHeader(value = "user", defaultValue = "sysUser") String user
  ) throws FXDefaultException {
    return amlRiskService.getAvailableCustomerRisk(customerCode, pageable, module, otherIdentity,
            from, to, user, tenent);
  }


  @PostMapping(path = "/{tenent}", produces = "application/json")
  public ResponseEntity<?> calculateRiskOnOnboarding(@PathVariable(value = "tenent") String tenent,
                                                     @RequestBody @Valid OnboardingCustomer customer,
                                                     @RequestHeader(value = "user", defaultValue
                                                             = "sysUser") String user
  ) throws FXDefaultException, IOException, ClassNotFoundException {
    Resource resource = new Resource(riskService.calcOnboardingRisk(customer, user, tenent));
    return ResponseEntity.ok(resource);
  }

  @GetMapping(path = "/{tenent}/calculate-one/{id}", produces = "application/json")
  public ResponseEntity<?> calculateRiskSingle(@PathVariable(value = "tenent") String tenent,
                                               @PathVariable(value = "id") Long id,
                                               @RequestHeader(value = "user", defaultValue =
                                                       "sysUser") String user
  ) throws FXDefaultException {
    Resource resource = new Resource(amlRiskService.calculateRiskByCustomer(user, tenent, id));
    return ResponseEntity.ok(resource);
  }

  @PostMapping(path = "/{tenent}/calculate", produces = "application/json")
  public ResponseEntity<?> calculateRiskBulk(@PathVariable(value = "tenent") String tenent,
                                             @RequestHeader(value = "user", defaultValue
                                                     = "sysUser") String user,
                                             @RequestParam(value = "pages", required = false)  Integer pageLimit
  ) throws FXDefaultException {

    logger.debug(String.format("Starting to calculate risk for the customer base. " +
            "User : %s , Tenent : %s", user, tenent));
    Resource resource = new Resource(riskService.calculateRiskForCustomerBase(user, tenent,pageLimit));
    return ResponseEntity.ok(resource);
  }
}

