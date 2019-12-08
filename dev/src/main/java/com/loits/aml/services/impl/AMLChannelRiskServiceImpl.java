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

  @Value("${global.date.format}")
  private String dateFormat;

  @Value("${aml.transaction.default.back-months}")
  private String DEFAULT_BACK_MONTHS_TRANSACTION;

  SimpleDateFormat sdf;

  @PostConstruct
  public void init() {
    this.sdf = new SimpleDateFormat(dateFormat);
  }


  public ChannelRisk calculateChannelRisk(Long customerId, Module module, String user,
                                          String tenent) throws
          FXDefaultException {
    logger.debug("Channel Risk calculation started...");
    ObjectMapper objectMapper = new ObjectMapper();
    List<Transaction> transactionList = null;
    ChannelRisk channelRisk = new ChannelRisk();

    //Get the transactions for customer
    //Request parameters to AML Service
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.MONTH, Integer.parseInt(DEFAULT_BACK_MONTHS_TRANSACTION));

    String amlServiceTransactionUrl = String.format(env.getProperty("aml.api.aml-transactions"),
            tenent);
    HashMap<String, String> transactionParameters = new HashMap<>();
    transactionParameters.put("customerProduct.customer.id", customerId.toString());
    transactionParameters.put("txnDate", sdf.format(cal.getTime()));

//        //Send request to Customer Service
//        ArrayList<Object> list = sendServiceRequest2(amlServiceTransactionUrl,
// transactionParameters, null, "AML");
//        try {
//            transactionList = objectMapper.convertValue(list, new
// TypeReference<List<Transaction>>() {
//            });
//        } catch (Exception e) {
//            logger.debug("Error in deserializing transactions from AML Service "+ e.getMessage());
//            e.printStackTrace();
//            channelRisk.setCalculatedRisk(0.0);
//            return channelRisk;
//        }

    try {
      transactionList = httpService.getDataFromList("AML", amlServiceTransactionUrl,
              transactionParameters, new TypeReference<List<Transaction>>() {
              });
    } catch (Exception e) {
      e.printStackTrace();
      channelRisk.setCalculatedRisk(0.0);
      return channelRisk;
      //throw new FXDefaultException("-1", "ERROR", "Exception occured in retrieving Customer
      // Products", new Date(), HttpStatus.BAD_REQUEST, false);
    }

    if (transactionList.size() > 0) {
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
          channelUsage.setAmount(t.getTxnAmount().doubleValue());

          channelUsageList.add(channelUsage);
        }
      }
      channelRisk.setModule(module);
      channelRisk.setChannelUsage(channelUsageList);
      channelRisk.setCustomerCode(customerId);
      channelRisk.setToday(new Timestamp(new Date().getTime()));

      //TODO add to HTTPService
      HashMap<String, String> headers = new HashMap<>();
      headers.put("user", user);
//            HttpResponse httpResponse = sendPostRequest(channelRisk, String.format(env
// .getProperty("aml.api.channel-risk"), tenent), "ChannelRisk", headers);
//
//            try {
//                String jsonString = EntityUtils.toString(httpResponse.getEntity());
//                channelRisk = objectMapper.readValue(jsonString, ChannelRisk.class);
//            } catch (IOException e) {
//                logger.debug("Error in deserializing channelRisk object");
//                e.printStackTrace();
//            }

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
