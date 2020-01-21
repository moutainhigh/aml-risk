package com.loits.aml.kafka.services.impl;

import com.loits.aml.domain.AmlRisk;
import com.loits.aml.domain.KafkaErrorLog;
import com.loits.aml.kafka.services.KafkaProducer;
import com.loits.aml.repo.KafkaErrorLogRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@Service
public class KafkaProducerImpl implements KafkaProducer {

  Logger logger = LogManager.getLogger(KafkaProducerImpl.class);

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  KafkaErrorLogRepository kafkaErrorLogRepository;

  @Override
  public void publishToTopic(String topic, AmlRisk amlRisk) {
    logger.debug("Publishing data to topic aml-risk-create started. Tenent: " + amlRisk.getTenent());
    try {
//      kafkaTemplate.send(topic, amlRisk);
      logger.debug("Publishing to kafka  successful for aml-risk with id " + amlRisk.getId());
    } catch (Exception e) {
      logError(e, topic, amlRisk);
      logger.debug("Publishing to kafka failed for aml-risk with id " + amlRisk.getId());
      e.printStackTrace();
    }
  }

  public void logError(Exception e, String topic, Object object) {
    KafkaErrorLog kafkaErrorLog = new KafkaErrorLog();
    kafkaErrorLog.setTimestamp(new Timestamp(new Date().getTime()));
    kafkaErrorLog.setErrorMessage(e.getMessage());
    kafkaErrorLog.setTopic(topic);
    kafkaErrorLog.setType("Producer");
    kafkaErrorLog.setSubType("Db");
    StringWriter stringWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stringWriter));
    kafkaErrorLog.setTrace(stringWriter.toString());
    kafkaErrorLog.setData(object.toString());
    kafkaErrorLogRepository.save(kafkaErrorLog);
  }
}
