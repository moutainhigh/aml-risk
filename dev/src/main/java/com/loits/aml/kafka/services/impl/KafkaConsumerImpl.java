package com.loits.aml.kafka.services.impl;

import com.loits.aml.domain.GeoLocation;
import com.loits.aml.domain.KafkaErrorLog;
import com.loits.aml.domain.Module;

import com.loits.aml.kafka.services.KafkaConsumer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.GeoLocationRepository;
import com.loits.aml.repo.KafkaErrorLogRepository;
import com.loits.aml.repo.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Date;

@Service
public class KafkaConsumerImpl implements KafkaConsumer {

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    GeoLocationRepository geolocationRepository;

    @Autowired
    KafkaErrorLogRepository kafkaErrorLogRepository;

    public void create(Module module){
        TenantHolder.setTenantId(module.getTenent());
        try{
            moduleRepository.save(module);
        }catch (Exception e){
            logError(e, "module-create", module);
        }
        TenantHolder.clear();
    }

    public void update(Module module){
        TenantHolder.setTenantId(module.getTenent());
        try{
            moduleRepository.save(module);
        }catch (Exception e){
            logError(e, "module-update", module);
        }
        TenantHolder.clear();
    }

    public void delete(Module module){
        TenantHolder.setTenantId(module.getTenent());
        try{
            moduleRepository.delete(module);
        }catch (Exception e){
            logError(e, "module-delete", module);
        }
        TenantHolder.clear();
    }

    public void create(GeoLocation geoLocation){
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.save(geoLocation);
        }catch (Exception e){
            logError(e, "geolocation-create", geoLocation);
        }
        TenantHolder.clear();
    }

    public void update(GeoLocation geoLocation){
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.save(geoLocation);
        }catch (Exception e){
            logError(e, "geolocation-update", geoLocation);
        }
        TenantHolder.clear();
    }

    public void delete(GeoLocation geoLocation){
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.delete(geoLocation);
        }catch (Exception e){
            logError(e, "geolocation-delete", geoLocation);
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
