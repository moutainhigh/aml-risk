package com.loits.aml.kafka.services.impl;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.AmlRisk;
import com.loits.aml.kafka.services.KafkaProducer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.KafkaErrorLogRepository;
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

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  KafkaErrorLogRepository kafkaErrorLogRepository;

  @Override
  public CompletableFuture<?> publishToTopic(String topic, AmlRisk amlRisk) throws FXDefaultException {
    return CompletableFuture.runAsync(() -> {
      TenantHolder.setTenantId(amlRisk.getTenent());
      kafkaTemplate.send(topic, amlRisk);
      TenantHolder.clear();
    });
  }
}
