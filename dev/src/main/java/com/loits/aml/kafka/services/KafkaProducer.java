package com.loits.aml.kafka.services;

import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.AmlRisk;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

public interface KafkaProducer {

  @Async
  CompletableFuture<?> publishToTopic(String topic, AmlRisk amlRisk);

}
