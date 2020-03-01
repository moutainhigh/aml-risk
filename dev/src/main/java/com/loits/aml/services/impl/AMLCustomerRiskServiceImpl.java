package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.*;
import com.loits.aml.dto.Transaction;
import com.loits.aml.services.AMLCustomerRiskService;
import com.loits.aml.services.HTTPService;
import com.loits.aml.services.KieService;
import com.loits.fx.aml.*;
import com.loits.fx.aml.Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AMLCustomerRiskServiceImpl implements AMLCustomerRiskService {

  Logger logger = LogManager.getLogger(AMLCustomerRiskServiceImpl.class);

  @Autowired
  KieService kieService;

  @Autowired
  Environment env;

  @Autowired
  HTTPService httpService;

  @Value("${global.date.format}")
  private String dateFormat;

  @Value("${aml.transaction.default.back-months}")
  private String DEFAULT_BACK_MONTHS_TRANSACTION;

  SimpleDateFormat sdf;

  @PostConstruct
  public void init() {
    this.sdf = new SimpleDateFormat(dateFormat);
  }

  @Override

  public CustomerRisk calculateCustomerRisk(Customer customer, Module ruleModule, String user,
                                            String tenent) throws FXDefaultException {
    ObjectMapper objectMapper = new ObjectMapper();

    //Set Customer details to a CustomerOnboarding object
    RiskCustomer riskCustomer = new RiskCustomer();

    try {
      riskCustomer.setId(customer.getId());
      for (CustomerMeta customerMeta : customer.getCustomerMetaList()) {
        if (customerMeta.getType().equalsIgnoreCase("clientCategory")) {
          riskCustomer.setClientCategory(customerMeta.getValue());
        }
        if (customerMeta.getType().equalsIgnoreCase("pepsEnabled")) {
          if (customerMeta.getValue().equalsIgnoreCase("Y")) {
            riskCustomer.setPepsEnabled(Byte.parseByte("1"));
          } else {
            riskCustomer.setPepsEnabled(Byte.parseByte("0"));
          }
        }
        if (customerMeta.getType().equalsIgnoreCase("withinBranchServiceArea")) {
          if (customerMeta.getValue().equalsIgnoreCase("Y")) {
            riskCustomer.setWithinBranchServiceArea(Byte.parseByte("1"));
          } else {
            riskCustomer.setWithinBranchServiceArea(Byte.parseByte("0"));
          }
        }
      }

      if (customer.getAnnualTurnoverFrom() != null) {
        riskCustomer.setAnnualTurnoverFrom(customer.getAnnualTurnoverFrom());
      }
      if (customer.getAnnualTurnoverTo() != null) {
        riskCustomer.setAnnualTurnoverTo(customer.getAnnualTurnoverTo());
      }
      if (customer.getAddresses() != null) {
        riskCustomer.setAddressesByCustomerCode((Collection<Address>) customer.getAddresses());
      }
      if (customer.getCustomerType() != null) {
        riskCustomer.setCustomerType(customer.getCustomerType().getCode());
        riskCustomer.setCustomerTypeId(customer.getCustomerType().getId());
      }

      if (customer.getIndustry() != null) {
        riskCustomer.setIndustry(customer.getIndustry().getIsoCode());
        riskCustomer.setIndustryId(customer.getIndustry().getId());
      }
      if (customer.getOccupation() != null) {
        riskCustomer.setOccupation(customer.getOccupation().getIsoCode());
        riskCustomer.setOccupationId(customer.getOccupation().getId());
      }
      riskCustomer.setModule(ruleModule);
    } catch (Exception e) {
      logger.debug("Failure in adding data to customer category risk calculation model");
      throw new FXDefaultException("3001", "INVALID_ATTEMPT", "Incomplete Customer Category data " +
              "for risk calculation", new Date(), HttpStatus.BAD_REQUEST, true);
    }

    //TODO change this to HTTP Service
    HashMap<String, String> headers = new HashMap<>();
    headers.put("user", user);
//        HttpResponse res = sendPostRequest(riskCustomer, String.format(env.getProperty("aml.api
// .category-risk"), tenent), "Aml-Category-Risk", headers);
//        CustomerRisk calculatedRisk = null;
//        try {
//            String jsonString = EntityUtils.toString(res.getEntity());
//            calculatedRisk = objectMapper.readValue(jsonString, CustomerRisk.class);
//            logger.debug("CustomerRisk calculated "+calculatedRisk);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    //Calculate customer category risk by sending request to Category Risk Service
    CustomerRisk customerRisk = null;
    try {
      customerRisk = (CustomerRisk) httpService.sendData("Category-risk",
              String.format(env.getProperty("aml.api.category-risk"), tenent),
              null, headers, CustomerRisk.class, riskCustomer);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    if (customerRisk.getCalculatedRisk() == null) {
      customerRisk.setCalculatedRisk(0.0);
    }
    return customerRisk;
  }
}
