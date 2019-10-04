package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Collection;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Occupation {
    private String type;
    private String isoCode;
    private String occupation;
    private Double riskScore;
    private Byte industryApplicable;
    private Byte highRisk;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private Collection<Customer> customers;
    private Occupation parent;
    private Collection<Occupation> occupationsById;
    private Module module;
}
