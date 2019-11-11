package com.loits.aml.kafka.services.impl;

import com.loits.aml.domain.GeoLocation;
import com.loits.aml.domain.KafkaErrorLog;
import com.loits.aml.domain.Module;

import com.loits.aml.kafka.services.KafkaConsumer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.GeoLocationRepository;
import com.loits.aml.repo.KafkaErrorLogRepository;
import com.loits.aml.repo.ModuleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Date;

@Service
public class KafkaConsumerImpl implements KafkaConsumer {
    Logger logger = LogManager.getLogger(KafkaConsumerImpl.class);

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    GeoLocationRepository geolocationRepository;

    @Autowired
    KafkaErrorLogRepository kafkaErrorLogRepository;

    public void create(Module module){
        logger.debug("Starting to create module from sync topic. Tenent : " + module.getTenent());
        TenantHolder.setTenantId(module.getTenent());
        try{
            moduleRepository.save(module);
            logger.debug("Module sync completed.");
        }catch (Exception e){
            logError(e, "module-create", module);
            e.printStackTrace();
            logger.debug("Module could not be synced.");
        }
        TenantHolder.clear();
    }

    public void update(Module module){
        logger.debug("Starting to update module from sync topic. Tenent : " + module.getTenent());
        TenantHolder.setTenantId(module.getTenent());
        try{
            moduleRepository.save(module);
            logger.debug("Module sync completed.");
        }catch (Exception e){
            logError(e, "module-update", module);
            e.printStackTrace();
            logger.debug("Module could not be synced.");
        }
        TenantHolder.clear();
    }

    public void delete(Module module){
        logger.debug("Starting to delete module from sync topic. Tenent : " + module.getTenent());
        TenantHolder.setTenantId(module.getTenent());
        try{
            moduleRepository.delete(module);
            logger.debug("Module sync completed.");
        }catch (Exception e){
            logError(e, "module-delete", module);
            e.printStackTrace();
            logger.debug("Module could not be synced.");
        }
        TenantHolder.clear();
    }

    public void create(GeoLocation geoLocation){
        logger.debug("Starting to create geo-location data, tenent "+geoLocation.getTenent() );
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.save(geoLocation);
            logger.debug("Kafka consumption successful for topic geolocation-create");
        }catch (Exception e){
            logError(e, "geolocation-create", geoLocation);
            e.printStackTrace();
            logger.debug("Kafka consumption failed for topic geolocation-create");
        }
        TenantHolder.clear();
    }

    public void update(GeoLocation geoLocation){
        logger.debug("Starting to update geo-location data, tenent "+geoLocation.getTenent() );
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.save(geoLocation);
            logger.debug("Kafka consumption successful for topic geolocation-update");
        }catch (Exception e){
            logError(e, "geolocation-update", geoLocation);
            e.printStackTrace();
            logger.debug("Kafka consumption failed for topic geolocation-update");
        }
        TenantHolder.clear();
    }

    public void delete(GeoLocation geoLocation){
        logger.debug("Starting to delete geo-location data, tenent "+geoLocation.getTenent() );
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.delete(geoLocation);
            logger.debug("Kafka consumption successful for topic geolocation-delete");
        }catch (Exception e){
            logError(e, "geolocation-delete", geoLocation);
            e.printStackTrace();
            logger.debug("Kafka consumption failed for topic geolocation-create");
        }
        TenantHolder.clear();
    }

    public void logError(Exception e, String topic, Object object){
        KafkaErrorLog kafkaErrorLog = new KafkaErrorLog();
        kafkaErrorLog.setTimestamp(new Timestamp(new Date().getTime()));
        kafkaErrorLog.setErrorMessage(e.getMessage());
        kafkaErrorLog.setTopic(topic);
        kafkaErrorLog.setType("Consumer");
        kafkaErrorLog.setSubType("Db");
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        kafkaErrorLog.setTrace(stringWriter.toString());
        kafkaErrorLog.setData(object.toString());
        kafkaErrorLogRepository.save(kafkaErrorLog);
    }

}
