package com.loits.aml.config;

import com.loits.aml.domain.Module;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    ModuleRepository moduleRepository;

    @KafkaListener(topics = "Module-test", groupId = "group_id")
    public void consume(Module module){
        TenantHolder.setTenantId("AnRkr");
        moduleRepository.save(module);
        TenantHolder.clear();
    }
}
