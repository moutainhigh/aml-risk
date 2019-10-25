package com.loits.aml.kafka.services;

import com.loits.aml.domain.GeoLocation;
import com.loits.aml.domain.Module;
import org.springframework.kafka.annotation.KafkaListener;

public interface KafkaConsumer {

    //Listeners for module
    @KafkaListener(topics = "module-create")
    void create(Module module);

    @KafkaListener(topics = "module-update")
    void update(Module module);

    @KafkaListener(topics = "module-delete")
    void delete(Module module);

    //Listeners for product
    @KafkaListener(topics = "geolocation-create")
    void create(GeoLocation geoLocation);

    @KafkaListener(topics = "geolocation-update")
    void update(GeoLocation geoLocation);

    @KafkaListener(topics = "geolocation-delete")
    void delete(GeoLocation geoLocation);
}
