package com.loits.aml.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Collection;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerType{
    private Long id;
    private String code;
    private String description;
    private Double riskScore;
    private Byte occupationApplicable;
    private Byte highRisk;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private Collection<Customer> customers;
    private Module module;
}
