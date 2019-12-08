package com.loits.aml.kafka.services;

import com.loits.aml.domain.AmlRisk;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

public interface KafkaProducer {

  void publishToTopic(String topic, AmlRisk amlRisk);

}
