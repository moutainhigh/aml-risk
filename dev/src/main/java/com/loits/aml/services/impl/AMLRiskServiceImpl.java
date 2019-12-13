package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.config.RestResponsePage;
import com.loits.aml.config.Translator;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.AmlRisk;
import com.loits.aml.dto.Customer;
import com.loits.aml.dto.CustomerRiskOutput;
import com.loits.aml.kafka.services.KafkaProducer;
import com.loits.aml.mt.TenantHolder;
import com.loits.aml.repo.*;
import com.loits.aml.services.*;
import com.loits.fx.aml.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AMLRiskServiceImpl implements AMLRiskService {

    Logger logger = LogManager.getLogger(AMLRiskServiceImpl.class);

    @Autowired
    KieService kieService;

    @Autowired
    AmlRiskRepository amlRiskRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    GeoLocationRepository geoLocationRepository;

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    Environment env;

    @Autowired
    HTTPService httpService;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ModuleCustomerRepository moduleCustomerRepository;

    @Autowired
    AMLChannelRiskService amlChannelRiskService;

    @Autowired
    AMLProductRiskService amlProductRiskService;

    @Autowired
    AMLCustomerRiskService amlCustomerRiskService;

    @Value("${global.date.format}")
    private String dateFormat;

    SimpleDateFormat sdf;

    @PostConstruct
    public void init() {
        this.sdf = new SimpleDateFormat(dateFormat);
    }


    @Override
    public Page<?> getAvailableCustomerRisk(String customerCode, Pageable pageable, String module,
                                            String otherIdentity, Date from, Date to, String user,
                                            String tenent) throws FXDefaultException {
        List<com.loits.aml.domain.ModuleCustomer> moduleCustomerList = null;
        com.loits.aml.domain.ModuleCustomer moduleCustomer = null;
        com.loits.aml.domain.Module moduleObj = null;
        List<CustomerRiskOutput> customerRiskOutputList = new ArrayList<>();
        int count = 0;

        if (!moduleRepository.existsById(module)) {
            throw new FXDefaultException("3001", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"),
                    new Date(), HttpStatus.BAD_REQUEST, false);
        } else {
            moduleObj = moduleRepository.findByCode(module).get();
        }

        if (from == null && to == null) {
            Date date = new GregorianCalendar(1970, Calendar.JANUARY, 1).getTime();
            from = new Date();
            to = new Date();
            from.setTime(date.getTime());
            to.setTime(new Date().getTime());
        }

        //Get risk of single customer
        if (customerCode != null && !customerCode.isEmpty()) {
            if (moduleCustomerRepository.existsByModuleAndModuleCustomerCode(moduleObj, customerCode)) {
                moduleCustomer =
                        moduleCustomerRepository.findOneByModuleAndModuleCustomerCodeAndRiskCalculatedOnBetween(moduleObj, customerCode, from, to);
            } else {
                throw new FXDefaultException("3003", "NO_DATA_FOUND", Translator.toLocale(
                        "CUSTOMER_NOT_FOUND"), new Date(), HttpStatus.BAD_REQUEST, false);
            }
            com.loits.aml.domain.Customer customer = new com.loits.aml.domain.Customer();
            if (moduleCustomer != null) {
                customer = moduleCustomer.getCustomer();
                if (customer != null) {
                    CustomerRiskOutput customerRiskOutput = new CustomerRiskOutput();
                    customerRiskOutput.setCustomerCode(moduleCustomer.getModuleCustomerCode());
                    if (moduleCustomer.getModule() != null) {
                        customerRiskOutput.setModule(moduleCustomer.getModule().getCode());
                    }
                    customerRiskOutput.setCalculatedRisk(customer.getCustomerRiskScore());
                    customerRiskOutput.setRiskRating(customer.getCustomerRisk());
                    customerRiskOutputList.add(customerRiskOutput);
                }
            }

            count = customerRiskOutputList.size();

        } else {
            if (moduleCustomerRepository.existsByModule(moduleObj)) {
                moduleCustomerList =
                        moduleCustomerRepository.findAllByModuleAndRiskCalculatedOnBetween(moduleObj,
                                from, to, pageable);
                count = moduleCustomerRepository.findCountByModuleAndRiskCalculatedOnBetween(moduleObj,
                        from, to);


//                QCustomer customer = QCustomer.customer;
//                BooleanBuilder bb = new BooleanBuilder();
//                bb.and(customer.moduleCustomers.contains(mc));
//
//
                for (com.loits.aml.domain.ModuleCustomer moduleCustomer1 : moduleCustomerList) {
                    com.loits.aml.domain.Customer customer = null;

                    if (moduleCustomer1 != null) {
                        customer = moduleCustomer1.getCustomer();
                        if (customer != null) {
                            CustomerRiskOutput customerRiskOutput = new CustomerRiskOutput();
                            customerRiskOutput.setCustomerCode(moduleCustomer1.getModuleCustomerCode());
                            if (moduleCustomer1.getModule() != null) {
                                customerRiskOutput.setModule(moduleCustomer1.getModule().getCode());
                            }
                            customerRiskOutput.setCalculatedRisk(customer.getCustomerRiskScore());
                            customerRiskOutput.setRiskRating(customer.getCustomerRisk());
                            customerRiskOutputList.add(customerRiskOutput);
                        }
                    }

                }
            } else {
                throw new FXDefaultException("3003", "NO_DATA_FOUND", Translator.toLocale(
                        "CUSTOMERS_NOT_FOUND"), new Date(), HttpStatus.BAD_REQUEST, false);
            }
        }

//            //Get last calculated risk of customer from AMLRisk table
//        List<AmlRisk> amlRiskList = new ArrayList<>();
//        List<OverallRisk> overallRiskList = new ArrayList<>();
//        if (customerCode != null && !customerCode.isEmpty()) {
//            amlRiskList = amlRiskRepository.findTopForCustomerOrderByCreatedOnDescBetween
// (customer.getId(), new Timestamp(from.getTime()), new Timestamp(to.getTime()));
//        } else if (customerCode == null || customerCode.isEmpty()) {
//            amlRiskList = amlRiskRepository.findTopForEachCustomerBetween(new Timestamp(from
// .getTime()), new Timestamp(to.getTime()));
//        } else {
//            throw new FXDefaultException("3003", "NO_DATA_AVAILABLE", Translator.toLocale
// ("NO_RISK_DATA"), new Date(), HttpStatus.BAD_REQUEST, false);
//        }
//
//        for (AmlRisk amlRisk : amlRiskList) {
//            OverallRisk overallRisk = new OverallRisk();
//            overallRisk.setCustomerCode(amlRisk.getCustomer());
//            overallRisk.setModule(ruleModule);
//            overallRisk.setRiskRating(amlRisk.getRiskRating());
//            overallRisk.setCalculatedRisk(amlRisk.getRisk());
//            overallRisk.setChannelRisk(amlRisk.getChannelRisk());
//            overallRisk.setProductRisk(amlRisk.getProductRisk());
//            overallRisk.setCalculatedRisk(amlRisk.getCalculatedRisk());
//            overallRiskList.add(overallRisk);
//        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), customerRiskOutputList.size());

        if (start <= end) {
            customerRiskOutputList = customerRiskOutputList.subList(start, end);
        }

        return new PageImpl<CustomerRiskOutput>(customerRiskOutputList,
                pageable, count);
    }


    public OverallRisk calculateRiskByCustomer(String user, String tenent, Long id) throws FXDefaultException {

        List<Customer> customerList = null;
        Customer customer = null;
        ObjectMapper objectMapper = new ObjectMapper();

        //Request parameters to Customer Service
        String customerServiceUrl = String.format(env.getProperty("aml.api.customer"), tenent);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", String.valueOf(id));

        try {
            customerList = httpService.getDataFromPage("Customer", customerServiceUrl, parameters,
                    new TypeReference<List<Customer>>() {
                    });
            customer = objectMapper.convertValue(customerList.get(0), Customer.class);
        } catch (Exception e) {
            throw new FXDefaultException("-1", "NO_DATA_FOUND", "No customers found", new Date(),
                    HttpStatus.BAD_REQUEST, false);
        }

        String module = "lending";//TODO customer.getCustomerModule().getModule();
        Module ruleModule = null;
        if (!moduleRepository.existsById(module)) {
            throw new FXDefaultException("-1", "INVALID_ATTEMPT", Translator.toLocale("FK_MODULE"),
                    new Date(), HttpStatus.BAD_REQUEST, false);
        } else {
            com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(module).get();
            ruleModule = new Module();
            ruleModule.setCode(dbModule.getCode());
            if (dbModule.getParent() != null) {
                Module ruleModuleParent = new Module();
                ruleModuleParent.setCode(dbModule.getParent().getCode());
                ruleModule.setParent(ruleModuleParent);
            }

            CustomerRisk customerRisk = this.amlCustomerRiskService
                    .calculateCustomerRisk(customer, ruleModule, user, tenent);

            ChannelRisk channelRisk = this.amlChannelRiskService
                    .calculateChannelRisk(customer.getId(), ruleModule, user,
                            tenent);

            ProductRisk productRisk = this.amlProductRiskService
                    .calculateProductRisk(customer.getId(), ruleModule, user,
                            tenent);

            if (customerRisk.getCalculatedRisk() != null) {
                if (channelRisk.getCalculatedRisk() == null) {
                    channelRisk.setCalculatedRisk(0.0);
                }
                if (productRisk.getCalculatedRisk() == null) {
                    productRisk.setCalculatedRisk(0.0);
                }

                if (customerRisk.getPepsEnabled() == null) {
                    customerRisk.setPepsEnabled(false);
                }

                if (customerRisk.getCustomerType() == null) {
                    CustomerType customerType = new CustomerType();
                    customerType.setHighRisk(false);
                    customerRisk.setCustomerType(customerType);
                }
                if (customerRisk.getOccupation() == null) {
                    Occupation occupation = new Occupation();
                    occupation.setHighRisk(false);
                    customerRisk.setOccupation(occupation);
                }

                if (channelRisk == null) {
                    channelRisk = new ChannelRisk();
                }

                if (productRisk == null) {
                    productRisk = new ProductRisk();
                }


                OverallRisk overallRisk = new OverallRisk(customer.getId(), ruleModule,
                        customerRisk.getCalculatedRisk(), productRisk.getCalculatedRisk(),
                        channelRisk.getCalculatedRisk(), customerRisk.getPepsEnabled(),
                        customerRisk.getCustomerType().getHighRisk(),
                        customerRisk.getOccupation().getHighRisk());
                overallRisk = kieService.getOverallRisk(overallRisk);

                //Save to calculated AmlRisk record to overallrisk
                saveRiskRecord(overallRisk, customerRisk.getId(), productRisk.getId(),
                        channelRisk.getId(), tenent, user, customer.getVersion(), module);

                return overallRisk;
            } else {
                throw new FXDefaultException();
            }
        }
    }

    @Override
    public boolean runRiskCronJob(Boolean calculateCustRisk, String user, String tenent,
                                  Customer customer) throws FXDefaultException {

        String module = "lofc";//TODO customer.getCustomerModule().getModule();
        Module ruleModule = null;
        if (!moduleRepository.existsById(module)) {
            logger.debug("Failure in assigning module to customer in risk calculation");
        } else {
            com.loits.aml.domain.Module dbModule = moduleRepository.findByCode(module).get();
            ruleModule = new Module();
            ruleModule.setCode(dbModule.getCode());
            if (dbModule.getParent() != null) {
                Module ruleModuleParent = new Module();
                ruleModuleParent.setCode(dbModule.getParent().getCode());
                ruleModule.setParent(ruleModuleParent);
            }

            CustomerRisk customerRisk = null;
            if (calculateCustRisk || !amlRiskRepository.existsByCustomer(customer.getId())) {
                customerRisk = this.amlCustomerRiskService.calculateCustomerRisk(customer, ruleModule,
                        user, tenent);
            } else {
                AmlRisk amlRisk =
                        amlRiskRepository.findTopByCustomerOrderByCreatedOnDesc(customer.getId()).get();
                customerRisk = new CustomerRisk();
                customerRisk.setOccupation(new Occupation());
                customerRisk.setCustomerType(new CustomerType());
                customerRisk.setCalculatedRisk(amlRisk.getCustomerRisk());
                customerRisk.setId(amlRisk.getCustomerRiskId());
                if (amlRisk.getRiskText() != null) {
                    if (amlRisk.getRiskText().contains("A politically exposed person")) {
                        customerRisk.setPepsEnabled(true);
                    }
                    customerRisk.setCustomerType(new CustomerType());
                    customerRisk.setOccupation(new Occupation());
                    if (amlRisk.getRiskText().contains("customer-type")) {
                        customerRisk.getCustomerType().setHighRisk(true);
                    }

                    if (amlRisk.getRiskText().contains("occupation")) {
                        customerRisk.getOccupation().setHighRisk(true);
                    }
                } else {
                    customerRisk.setPepsEnabled(false);
                    customerRisk.getCustomerType().setHighRisk(false);
                    customerRisk.getOccupation().setHighRisk(false);
                }

                //TODO change approach to save the booleans in amlrisk and retrieve
            }

            ChannelRisk channelRisk = this.amlChannelRiskService.calculateChannelRisk
                    (customer.getId(), ruleModule, user, tenent);

            ProductRisk productRisk = this.amlProductRiskService.calculateProductRisk
                    (customer.getId(), ruleModule, user, tenent);

            if (customerRisk.getCalculatedRisk() != null) {
                if (channelRisk.getCalculatedRisk() == null) {
                    channelRisk.setCalculatedRisk(0.0);
                }
                if (productRisk.getCalculatedRisk() == null) {
                    productRisk.setCalculatedRisk(0.0);
                }
                if (customerRisk.getPepsEnabled() == null) {
                    customerRisk.setPepsEnabled(false);
                }

                if (customerRisk.getCustomerType() == null) {
                    CustomerType customerType = new CustomerType();
                    customerType.setHighRisk(false);
                    customerRisk.setCustomerType(customerType);
                }
                if (customerRisk.getOccupation() == null) {
                    Occupation occupation = new Occupation();
                    occupation.setHighRisk(false);
                    customerRisk.setOccupation(occupation);
                }

                if (channelRisk == null) {
                    channelRisk = new ChannelRisk();
                }

                if (productRisk == null) {
                    productRisk = new ProductRisk();
                }

                OverallRisk overallRisk = new OverallRisk(customer.getId(), ruleModule,
                        customerRisk.getCalculatedRisk(), productRisk.getCalculatedRisk(),
                        channelRisk.getCalculatedRisk(), customerRisk.getPepsEnabled(),
                        customerRisk.getCustomerType().getHighRisk(),
                        customerRisk.getOccupation().getHighRisk());

                overallRisk = kieService.getOverallRisk(overallRisk);

                //Save AMLRISK record
                AmlRisk risk = saveRiskRecord(overallRisk, customerRisk.getId(), productRisk.getId(),
                        channelRisk.getId(), tenent, user, customer.getVersion(), module);

                if (risk != null) {
                    logger.debug("New risk has been calculated for given customer");
                    return true;
                } else {
                    logger.debug("New risk has been calculated for given customer");
                }
            } else {
                logger.debug("Failure in calculating risk for Customer with id " + customer.getId());
            }
        }
        return false;
    }

    AmlRisk saveRiskRecord(OverallRisk overallRisk, Long customerRiskId,
                           Long productRiskId, Long channelRiskId, String tenent,
                           String user, Long version, String module) throws FXDefaultException {
        logger.debug("AmlRisk record save stared");
        AmlRisk amlRisk;

        if (amlRiskRepository.existsByCustomer(overallRisk.getCustomerCode())) {
            AmlRisk existingAmlRisk =
                    amlRiskRepository.findTopByCustomerOrderByCreatedOnDesc(overallRisk.getCustomerCode()).get();

            if (existingAmlRisk.getRisk().equals(overallRisk.getCalculatedRisk()) &&
                    existingAmlRisk.getRiskRating().equalsIgnoreCase(overallRisk.getRiskRating())) {
                logger.debug("Calculated AmlRisk equal to last calculated risk. Aborting AmlRisk save " +
                        "process...");
                amlRisk = null;
            } else {
                logger.debug("Calculated AmlRisk not equal to last calculated risk. Continuing AmlRisk " +
                        "save process...");
                amlRisk = new AmlRisk();
                Timestamp riskCalcOn = new Timestamp(new Date().getTime());
                amlRisk.setCreatedOn(riskCalcOn);
                amlRisk.setCreatedBy(user);
                amlRisk.setRiskRating(overallRisk.getRiskRating());
                amlRisk.setCustomerRisk(overallRisk.getCustomerRisk());
                amlRisk.setChannelRisk(overallRisk.getChannelRisk());
                amlRisk.setProductRisk(overallRisk.getProductRisk());
                amlRisk.setRisk(overallRisk.getCalculatedRisk());
                amlRisk.setCustomerRiskId(customerRiskId);
                amlRisk.setChannelRiskId(channelRiskId);
                amlRisk.setProductRiskId(productRiskId);
                amlRisk.setTenent(tenent);
                amlRisk.setCustomer(overallRisk.getCustomerCode());
                amlRisk.setRiskCalculationStatus(version);
                amlRisk.setModule(module);
                StringBuilder stringBuilder = new StringBuilder();

                if (overallRisk.getPepsEnabled()) {
                    stringBuilder.append("A politically exposed person");
                }
                if (overallRisk.getHighRiskCustomerType()) {
                    if (stringBuilder.length() != 0) {
                        stringBuilder.append(" with");
                    } else {
                        stringBuilder.append("Customer has");
                    }
                    stringBuilder.append(" a high risk customer-type");
                } else {

                }
                if (overallRisk.getHighRiskOccupation()) {
                    if (stringBuilder.length() != 0) {
                        stringBuilder.append(" and");
                    } else {
                        stringBuilder.append("Customer has");
                    }
                    stringBuilder.append(" a high risk occupation");
                }

                amlRisk.setRiskText(stringBuilder.toString());

                //Temporary placeholder
                amlRisk.setTenent(tenent);

                try {
                    amlRisk = amlRiskRepository.save(amlRisk);
                    logger.debug("AmlRisk record saved to database successfully");

                    //Publish record to Kafka
                    kafkaProducer.publishToTopic("aml-risk-create", amlRisk);
                    saveRiskCalculationTime(overallRisk.getCustomerCode(), riskCalcOn, tenent);
                    //save risk calculation time in customer

                } catch (Exception e) {
                    logger.debug("AmlRisk record save failed");
                }
            }
        } else {
            amlRisk = new AmlRisk();
            Timestamp riskCalcOn = new Timestamp(new Date().getTime());
            amlRisk.setCreatedOn(riskCalcOn);
            amlRisk.setCreatedBy(user);
            amlRisk.setRiskRating(overallRisk.getRiskRating());
            amlRisk.setCustomerRisk(overallRisk.getCustomerRisk());
            amlRisk.setChannelRisk(overallRisk.getChannelRisk());
            amlRisk.setProductRisk(overallRisk.getProductRisk());
            amlRisk.setRisk(overallRisk.getCalculatedRisk());
            amlRisk.setCustomerRiskId(customerRiskId);
            amlRisk.setChannelRiskId(channelRiskId);
            amlRisk.setProductRiskId(productRiskId);
            amlRisk.setTenent(tenent);
            amlRisk.setCustomer(overallRisk.getCustomerCode());
            amlRisk.setRiskCalculationStatus(version);
            amlRisk.setModule(module);
            StringBuilder stringBuilder = new StringBuilder();

            if (overallRisk.getPepsEnabled()) {
                stringBuilder.append("A politically exposed person");
            }
            if (overallRisk.getHighRiskCustomerType()) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(" with");
                } else {
                    stringBuilder.append("Customer has");
                }
                stringBuilder.append(" a high risk customer-type");
            } else {

            }
            if (overallRisk.getHighRiskOccupation()) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(" and");
                } else {
                    stringBuilder.append("Customer has");
                }
                stringBuilder.append(" a high risk occupation");
            }

            amlRisk.setRiskText(stringBuilder.toString());

            //Temporary placeholder
            amlRisk.setTenent(tenent);

            try {
                amlRisk = amlRiskRepository.save(amlRisk);
                logger.debug("AmlRisk record saved to database successfully");

                //Publish record to Kafka
                kafkaProducer.publishToTopic("aml-risk-create", amlRisk);
                saveRiskCalculationTime(overallRisk.getCustomerCode(), riskCalcOn, tenent);
                //save risk calculation time in customer

            } catch (Exception e) {
                logger.debug("AmlRisk record save failed");
            }
        }
        return amlRisk;
    }

    void saveRiskCalculationTime(Long customerId, Timestamp riskCalcOn,
                                 String tenent) {

        logger.debug("Saving risk calculatedOn time in customer and module customers for " + customerId + " with tenant " + tenent);
        TenantHolder.setTenantId(tenent);
        if (customerRepository.existsById(customerId)) {
            com.loits.aml.domain.Customer customer = customerRepository.findById(customerId).get();
            customer.setRiskCalculatedOn(riskCalcOn);
//                List<com.loits.aml.domain.ModuleCustomer> moduleCustomerList = customer
// .getModuleCustomers();
//                for (com.loits.aml.domain.ModuleCustomer moduleCustomer:moduleCustomerList) {
//                    moduleCustomer.setRiskCalculatedOn(riskCalcOn);
//                }
            try {
                customerRepository.save(customer);
                logger.debug("Saving risk calculatedOn time in customer with id " + customerId + " " +
                        "successful");
            } catch (Exception e) {
                logger.debug("Saving risk calculatedOn time in customer with id " + customerId + " " +
                        "failed");
            }
        } else {
            logger.debug("Saving risk calculatedOn time in customer with id " + customerId + " failed" +
                    ". Customer not available");
        }
    }
}
