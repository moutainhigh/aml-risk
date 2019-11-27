package com.loits.aml.kafka.services.impl;

import com.loits.aml.domain.Customer;
import com.loits.aml.domain.GeoLocation;
import com.loits.aml.domain.KafkaErrorLog;
import com.loits.aml.domain.Module;

import com.loits.aml.kafka.services.KafkaConsumer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.CustomerRepository;
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

    @Autowired
    CustomerRepository customerRepository;

    public void create(Module module) {
        logger.debug("Module Create Sync started for topic module-create with tenent " + module.getTenent());
        TenantHolder.setTenantId(module.getTenent());
        try {
            moduleRepository.save(module);
            logger.debug("Module sync completed for module with code "+module.getCode());
        } catch (Exception e) {
            logError(e, "module-create", module);
            e.printStackTrace();
            logger.debug("Module could not be synced for module with code "+module.getCode());
        }
        TenantHolder.clear();
    }

    public void update(Module module) {
        logger.debug("Module Update Sync started topic module-update with tenent " + module.getTenent());
        TenantHolder.setTenantId(module.getTenent());
        try {
            moduleRepository.save(module);
            logger.debug("Module sync completed for module with code "+module.getCode());
        } catch (Exception e) {
            logError(e, "module-update", module);
            e.printStackTrace();
            logger.debug("Module could not be synced for module with code "+module.getCode());
        }
        TenantHolder.clear();
    }

    public void delete(Module module) {
        logger.debug("Module Delete Sync started topic module-delete with tenent " + module.getTenent());
        TenantHolder.setTenantId(module.getTenent());
        try {
            moduleRepository.deleteById(module.getCode());
            logger.debug("Module sync completed for module with code "+module.getCode());
        } catch (Exception e) {
            logError(e, "module-delete", module);
            e.printStackTrace();
            logger.debug("Module could not be synced for module with code "+module.getCode());
        }
        TenantHolder.clear();
    }

    public void create(GeoLocation geoLocation){
        logger.debug("Starting to create geo-location data, tenent "+geoLocation.getTenent() );
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.save(geoLocation);
            logger.debug("Kafka consumption successful for geolocation with id "+geoLocation.getId());
        }catch (Exception e){
            logError(e, "geolocation-create", geoLocation);
            logger.debug("Kafka consumption failed for for geolocation with id "+geoLocation.getId());
            e.printStackTrace();

        }
        TenantHolder.clear();
    }

    public void update(GeoLocation geoLocation){
        logger.debug("Starting to update geo-location data, tenent "+geoLocation.getTenent() );
        TenantHolder.setTenantId(geoLocation.getTenent());
        try{
            geolocationRepository.save(geoLocation);
            logger.debug("Kafka consumption successful for geolocation with id "+geoLocation.getId());
        }catch (Exception e){
            logError(e, "geolocation-update", geoLocation);
            logger.debug("Kafka consumption failed for geolocation with id "+geoLocation.getId());
            e.printStackTrace();
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

    public void create(Customer customer) {
        TenantHolder.setTenantId(customer.getTenent());
        logger.debug("Starting to create new customer from sync topic. Tenent : " + customer.getTenent());

        processCustomer(false, customer);
        try {
            customerRepository.save(customer);
            logger.debug("Customer sync completed for customer id " + customer.getId());
        } catch (Exception e) {
            logger.debug("Customer could not be synced for customer id " + customer.getId());
            e.printStackTrace();
            logError(e, "customer-create", customer);
        }
        TenantHolder.clear();
    }

    public void update(Customer customer) {
        TenantHolder.setTenantId(customer.getTenent());
        logger.debug("Starting to update customer from sync topic. Tenent : " + customer.getTenent());
        processCustomer(true, customer);

        try {
            customerRepository.save(customer);
            logger.debug("Customer sync completed for customer id " + customer.getId());
        } catch (Exception e) {
            logger.debug("Customer could not be synced for customer id " + customer.getId());
            e.printStackTrace();
            logError(e, "customer-update", customer);
        }
        TenantHolder.clear();
    }

    public void delete(Customer customer) {
        TenantHolder.setTenantId(customer.getTenent());
        logger.debug("Starting to delete customer from sync topic. Tenent : " + customer.getTenent());
        try {
            customerRepository.delete(customer);
            logger.debug("Customer sync completed for customer id " + customer.getId());
        } catch (Exception e) {
            logger.debug("Customer could not be synced for customer id " + customer.getId());
            e.printStackTrace();
            logError(e, "customer-delete", customer);
        }
        TenantHolder.clear();
    }

    void processCustomer(boolean update, Customer customer) {

        if(update){
            Customer existingCustomer = customerRepository.findById(customer.getId()).get();
            customer.setRiskCalculatedOn(existingCustomer.getRiskCalculatedOn());
        }
        if (customer.getCustomerIdentificationList() != null)
            customer.getCustomerIdentificationList().forEach(customerIdentification -> customerIdentification.setCustomer(customer));
        if (customer.getModuleCustomers() != null) {
            customer.getModuleCustomers().forEach(moduleCustomer -> moduleCustomer.setCustomer(customer));
            if(update && customerRepository.existsById(customer.getId())) {
                Customer existingCustomer = customerRepository.findById(customer.getId()).get(); //TODO remove
                customer.getModuleCustomers().forEach(moduleCustomer -> moduleCustomer.setRiskCalculatedOn(existingCustomer.getRiskCalculatedOn()));
            }
        }
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
