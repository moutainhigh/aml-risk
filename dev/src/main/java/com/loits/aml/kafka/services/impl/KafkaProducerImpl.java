package com.loits.aml.kafka.services.impl;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.AmlRisk;
import com.loits.aml.kafka.services.KafkaProducer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.KafkaErrorLogRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

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
  public CompletableFuture<?> publishToTopic(String topic, AmlRisk amlRisk) {
    return CompletableFuture.runAsync(() -> {
      logger.debug("Publishing data to topic aml-risk-create started");
      TenantHolder.setTenantId(amlRisk.getTenent());
      try {
        kafkaTemplate.send(topic, amlRisk);
        logger.debug("Publishing to topic aml-risk-create successful");
      }catch(Exception e){
        logger.debug("Publishing to topic aml-risk-create failed");
      }
      TenantHolder.clear();
    });
  }
}
