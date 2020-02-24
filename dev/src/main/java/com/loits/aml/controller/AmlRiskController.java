package com.loits.aml.controller;

import com.loits.aml.commons.RiskCalcParams;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.OnboardingCustomer;
import com.loits.aml.services.AMLRiskService;
import com.loits.aml.services.KieService;
import com.loits.aml.services.RiskService;
import com.loits.aml.services.SegmentedRiskService;
import com.loits.fx.aml.OverallRisk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

  @Autowired
  SegmentedRiskService segmentedRiskService;

  @Autowired
  KieService kieService;

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
    logger.debug("Starting to retrieve available customer risks...");
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
                                                       "sysUser") String user,
                                               @RequestParam(name = "projection",
                                                       defaultValue = "defaultProjection") String projection
  ) throws FXDefaultException {
    Resource resource = new Resource(amlRiskService.calculateRiskByCustomer(user, tenent, id, projection));
    return ResponseEntity.ok(resource);
  }


  @PostMapping(path = "/{tenent}/calculate-many", produces = "application/json")
  public ResponseEntity<?> calculateRiskSingle(@PathVariable(value = "tenent") String tenent,
                                               @RequestHeader(value = "user", defaultValue =
                                                       "sysUser") String user,
                                               @RequestBody List<OverallRisk> customers
  ) throws FXDefaultException, ExecutionException, InterruptedException {
    Resources resource = new Resources(riskService.calculateForModuleCustomers(user, tenent, customers));
    return ResponseEntity.ok(resource);
  }



  @PostMapping(path = "/{tenent}/calculate", produces = "application/json")
  public ResponseEntity<?> calculateRiskBulk(@PathVariable(value = "tenent") String tenent,
                                             @RequestHeader(value = "user", defaultValue
                                                     = "sysUser") String user,
                                             @RequestParam(name = "projection",
                                                     defaultValue = "not-applicable") String projection,
                                              RiskCalcParams riskCalcParams) throws FXDefaultException {

    logger.debug(String.format("Starting to calculate risk for the customer base. " +
            "User : %s , Tenent : %s, Params : %s", user, tenent, riskCalcParams.toString()));
    Resource resource = new Resource(riskService.calculateRiskForCustomerBase(projection,user, tenent,
            riskCalcParams));
    return ResponseEntity.ok(resource);
  }

  @GetMapping(path = "/{tenent}/calculate-2", produces = "application/json")
  public ResponseEntity<?> calculateRiskBatch(@PathVariable(value = "tenent") String tenent,
                                              @RequestHeader(value = "user", defaultValue =
                                                      "sysUser") String user,
                                              RiskCalcParams riskCalcParams
  ) throws FXDefaultException {
    logger.debug(String.format("Starting to calculate risk for the customer base in basic looping" +
            " mode. User : %s , Tenent : %s, Params : %s", user, tenent,
            riskCalcParams.toString()));

    Resource resource = new Resource(segmentedRiskService.calculateRiskForBatch(user, tenent,riskCalcParams));
    return ResponseEntity.ok(resource);
  }

}

