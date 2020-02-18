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
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
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
import java.util.Collection;

@Service
public class KieServiceImpl implements KieService {

    private static KieContainer kieContainer;
    private static KieServices kieServices;
    private StatelessKieSession kieSession;

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
        kieServices = KieServices.get();
        kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newStatelessKieSession("kie-session");

    }

    @Override
    public OverallRisk getOverallRisk(OverallRisk overallRisk) throws FXDefaultException {

        OverallRisk calculatedOverallRisk = null;

        KieCommands commandsFactory = KieServices.Factory.get().getCommands();
        BatchExecutionCommandImpl command = new BatchExecutionCommandImpl();

        //Create Commands
        FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();
        //Insert channels to kiesession

        command.addCommand(new InsertObjectCommand(overallRisk));
        command.addCommand(fireAllRulesCommand);
        command.addCommand(commandsFactory.newGetObjects("OverallRisk"));
        try {

            logger.debug("Sending component risks to the rule engine to calculate Overall Risk");
            //Sending request to the rule engine to calculate overall risk
            ExecutionResults response =
                    kieSession.execute(command);

            if (response == null) {
                logger.debug("Overall Risk calculation failed from rule engine for customer " + overallRisk.getCustomerCode());
                return overallRisk;
            } else {
                logger.debug("Overall Risk calculation Successful from the rule engine");
                ArrayList obj = (ArrayList) response.getValue("OverallRisk");
                calculatedOverallRisk = (OverallRisk) obj.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Unable to get response from the rule engine");
            calculatedOverallRisk = overallRisk;
        } finally {
            return calculatedOverallRisk;
        }
    }
}
