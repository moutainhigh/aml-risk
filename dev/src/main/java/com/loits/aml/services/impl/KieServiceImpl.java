package com.loits.aml.services.impl;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.services.KieService;
import com.loits.fx.aml.OverallRisk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class KieServiceImpl implements KieService {

  private static KieServicesConfiguration conf;
  private static KieServicesClient kieServicesClient;
  private static final MarshallingFormat FORMAT = MarshallingFormat.JSON;

  Logger logger = LogManager.getLogger(KieServiceImpl.class);

  @Value("${loits.aml.pam.url}")
  private String redhatServerUrl;

  @Value("${loits.aml.pam.username}")
  private String username;

  @Value("${loits.aml.pam.password}")
  private String password;

  @Value("${loits.aml.pam.container}")
  private String containerId;

  @Value("${loits.aml.pam.enable}")
  private String ENABLE_PAM;


  @PostConstruct
  public void init() {
    // Connect to the RedHat Server
    if (ENABLE_PAM != null && ENABLE_PAM.equalsIgnoreCase("true")) {
      conf = KieServicesFactory.newRestConfiguration(redhatServerUrl, username, password, 60000);
      conf.setMarshallingFormat(FORMAT);
      kieServicesClient = KieServicesFactory.newKieServicesClient(conf);
    }
  }

  @Override
  public OverallRisk getOverallRisk(OverallRisk overallRisk) throws FXDefaultException {

    OverallRisk calculatedOverallRisk = null;
    //Kie API
    RuleServicesClient rulesClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
    KieCommands commandsFactory = KieServices.Factory.get().getCommands();
    BatchExecutionCommandImpl command = new BatchExecutionCommandImpl();
    command.setLookup("kie-session");

    //Create Commands
    FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();
    //Insert channels to kiesession

    command.addCommand(new InsertObjectCommand(overallRisk));
    command.addCommand(fireAllRulesCommand);
    command.addCommand(commandsFactory.newGetObjects("OverallRisk"));

    try {

      logger.debug("Sending component risks to the rule engine to calculate Overall Risk");
      //Sending request to the rule engine to calculate overall risk
      ServiceResponse<ExecutionResults> response =
              rulesClient.executeCommandsWithResults(containerId, command);

      if (!KieServiceResponse.ResponseType.SUCCESS.equals(response.getType())) {
        logger.debug("Overall Risk calculation failed from the rulRisk calculation for pagee engine for customer " + overallRisk.getCustomerCode() + " with message " + response.getMsg());
        return overallRisk;
      } else {
        logger.debug("Overall Risk calculation Successful from the rule engine");
        ArrayList obj = (ArrayList) response.getResult().getValue("OverallRisk");
        calculatedOverallRisk = (OverallRisk) obj.get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.debug("Unable to get response from the rule engine");
      calculatedOverallRisk = overallRisk;
    } finally {
      kieServicesClient.close();
      conf.dispose();
      return calculatedOverallRisk;
    }
  }
}
