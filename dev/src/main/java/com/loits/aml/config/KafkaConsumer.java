package com.loits.aml.config;

import com.loits.aml.domain.Customer;
import com.loits.aml.domain.Module;
import com.loits.aml.domain.Product;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.CustomerRepository;
import com.loits.aml.repo.ModuleRepository;
import com.loits.aml.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CustomerRepository customerRepository;

    //Listeners for module
    @KafkaListener(topics = "module-create", groupId = "group_id")
    public void create(Module module){
        TenantHolder.setTenantId("AnRkr");
        moduleRepository.save(module);
        TenantHolder.clear();
    }

    @KafkaListener(topics = "module-update", groupId = "group_id")
    public void update(Module module){
        TenantHolder.setTenantId("AnRkr");
        moduleRepository.save(module);
        TenantHolder.clear();
    }

    @KafkaListener(topics = "module-delete", groupId = "group_id")
    public void delete(Module module){
        TenantHolder.setTenantId("AnRkr");
        moduleRepository.delete(module);
        TenantHolder.clear();
    }

    //Listeners for product
    @KafkaListener(topics = "product-create", groupId = "group_id")
    public void create(Product product){
        TenantHolder.setTenantId("AnRkr");
        productRepository.save(product);
        TenantHolder.clear();
    }

    @KafkaListener(topics = "product-update", groupId = "group_id")
    public void update(Product product){
        TenantHolder.setTenantId("AnRkr");
        productRepository.save(product);
        TenantHolder.clear();
    }

    @KafkaListener(topics = "product-delete", groupId = "group_id")
    public void delete(Product product){
        TenantHolder.setTenantId("AnRkr");
        productRepository.delete(product);
        TenantHolder.clear();
    }

    //Listeners for customer
    @KafkaListener(topics = "customer-create" , groupId = "group_id")
    public void create(Customer customer){
        TenantHolder.setTenantId("AnRkr");
        customerRepository.save(customer);
        TenantHolder.clear();
    }

    @KafkaListener(topics = "customer-update", groupId = "group_id")
    public void update(Customer customer){
        TenantHolder.setTenantId("AnRkr");
        customerRepository.save(customer);
        TenantHolder.clear();
    }

    @KafkaListener(topics = "customer-delete", groupId = "group_id")
    public void delete(Customer customer){
        TenantHolder.setTenantId("AnRkr");
        customerRepository.delete(customer);
        TenantHolder.clear();
    }
}
