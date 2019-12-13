package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.Transaction;
import com.loits.aml.services.AMLChannelRiskService;
import com.loits.aml.services.HTTPService;
import com.loits.aml.services.KieService;
import com.loits.fx.aml.ChannelRisk;
import com.loits.fx.aml.ChannelUsage;
import com.loits.fx.aml.Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AMLChannelRiskServiceImpl implements AMLChannelRiskService {

  Logger logger = LogManager.getLogger(AMLChannelRiskServiceImpl.class);


  @Autowired
  Environment env;

  @Autowired
  KieService kieService;

  @Autowired
  HTTPService httpService;


  public ChannelRisk calculateChannelRisk(Long customerId, Module module, String user,
                                          String tenent, List<Transaction> transactionList) throws
          FXDefaultException {
    logger.debug("Channel Risk calculation started...");
    ObjectMapper objectMapper = new ObjectMapper();
    ChannelRisk channelRisk = new ChannelRisk();

    if (transactionList!=null && transactionList.size() > 0) {
      logger.debug("Transactions available. Starting calculation...");
      //send Transaction list to ChannelRisk
      List<ChannelUsage> channelUsageList = new ArrayList<>();

      for (Transaction t : transactionList) {
        if (t.getChannel() != null) {
          ChannelUsage channelUsage = new ChannelUsage();
          channelUsage.setChannelId(t.getChannel().getId());
          channelUsage.setChannel(t.getChannel().getCode());
          channelUsage.setChannelName(t.getChannel().getChannelName());
          channelUsage.setChannelDescription(t.getChannel().getChannelDescription());
          channelUsage.setDate(t.getTxnDate());
          if(t.getTxnAmount()!=null){
            channelUsage.setAmount(t.getTxnAmount().doubleValue());
          }
          channelUsageList.add(channelUsage);
        }
      }
      channelRisk.setModule(module);
      channelRisk.setChannelUsage(channelUsageList);
      channelRisk.setCustomerCode(customerId);
      channelRisk.setToday(new Timestamp(new Date().getTime()));

      HashMap<String, String> headers = new HashMap<>();
      headers.put("user", user);
      //Calculate customer category risk by sending request to Category Risk Service
      try {
        channelRisk = (ChannelRisk) httpService.sendData("Channel-risk",
                String.format(env.getProperty("aml.api.channel-risk"), tenent),
                null, headers, ChannelRisk.class, channelRisk);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }

    } else {
      logger.debug("No transactions available to calculate Channel Risk. Aborting...");
      channelRisk.setCalculatedRisk(0.0);
    }
    logger.debug("ChannelRisk obj " + channelRisk);
    return channelRisk;
  }

}
