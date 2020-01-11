package com.loits.aml.config;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

//@EnableKafka
//@Configuration
public class KafkaConfiguration {

    @Value("${loits.fusionx.kafka.url}")
    private String kafkaUrl;

    @Value("${com.loits.aml.kafka.acks}")
    private String acks;

    @Value("${com.loits.aml.kafka.retries}")
    private Integer retries;

    @Value("${com.loits.aml.kafka.batch.size}")
    private Integer batchSize;

    @Value("${com.loits.aml.kafka.linger.ms}")
    private Integer lingerMs;

    @Value("${com.loits.aml.kafka.buffer.memory}")
    private Long bufferMemory;

    @Value("${com.loits.aml.kafka.aml-risk.consumergroup}")
    private String consumerGroup;

    @Value("${com.loits.aml.kafka.trustedpackages}")
    private String deserializablePackages;

    /*--- kafka- stream---*/
    @Value("${com.loits.aml.kafka.authToken}")
    private String authToken;

    @Value("${com.loits.aml.kafka.tenancyName}")
    private String tenancyName;

    @Value("${com.loits.aml.kafka.username}")
    private String username;

    @Value("${com.loits.aml.kafka.streampoolId}")
    private String streampoolId;

    @Value("${com.loits.aml.kafka.securityProtocol}")
    private String securityProtocol;

    @Value("${com.loits.aml.kafka.saslMechanism}")
    private String saslMechanism;

    @Value("${com.loits.aml.kafka.topicsAutoCreate}")
    private Boolean topicConfig;
    /*--- kafka- stream---*/

    /**
     * Publisher
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(){
        Map<String,Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put("acks", acks);
        config.put("retries", retries);
        config.put("batch.size", batchSize);
        config.put("linger.ms", lingerMs);
        config.put("buffer.memory", bufferMemory);
        config.put("security.protocol", securityProtocol);
        config.put("sasl.mechanism", saslMechanism);
        config.put("auto.create.topics.enable", topicConfig);

        config.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                        + tenancyName + "/"
                        + username + "/"
                        + streampoolId + "\" "
                        + "password=\""
                        + authToken + "\";"
        );
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String,Object> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }


    /**
     * Consumer
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory(){
        Map<String,Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, deserializablePackages);
        config.put("security.protocol", securityProtocol);
        config.put("sasl.mechanism", saslMechanism);
        config.put("auto.create.topics.enable", topicConfig);

        config.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                        + tenancyName + "/"
                        + username + "/"
                        + streampoolId + "\" "
                        + "password=\""
                        + authToken + "\";"
        );
        return new DefaultKafkaConsumerFactory<String, Object>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
