package com.loits.aml.services.impl;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.services.KieService;
import com.redhat.aml.OverallRisk;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
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

    @Value("${loits.aml.pam.url}")
    private String redhatServerUrl;

    @Value("${loits.aml.pam.username}")
    private String username;

    @Value("${loits.aml.pam.password}")
    private String password;

    @Value("${loits.aml.pam.container}")
    private String containerId;

    @PostConstruct
    public void init(){
        //Connect to the RedHat Server
        conf = KieServicesFactory.newRestConfiguration(redhatServerUrl, username, password);
        conf.setMarshallingFormat(FORMAT);
        kieServicesClient = KieServicesFactory.newKieServicesClient(conf);
    }

    @Override
    public OverallRisk getOverallRisk(OverallRisk overallRisk) throws FXDefaultException {
        OverallRisk calculatedOverallRisk = null;
        //Kie API
        System.out.println("== Sending commands to the server ==");
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

        ServiceResponse<ExecutionResults> response = rulesClient.executeCommandsWithResults(containerId, command);

        if (response.getType().toString().equals("FAILURE")) {
            new FXDefaultException();
        } else {
            ArrayList obj = (ArrayList) response.getResult().getValue("OverallRisk");
            calculatedOverallRisk = (OverallRisk) obj.get(0);
        }
        return calculatedOverallRisk;
    }
}
