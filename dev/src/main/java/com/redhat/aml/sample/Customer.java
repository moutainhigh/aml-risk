package com.redhat.aml.sample;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.aml.core.BaseEntity;
import com.redhat.aml.Address;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Collection;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private Long id;
    private String surname;
    private String otherNames;
    private String title;
    private String nic;
    private String oldNic;
    private String clientCategory;
    private Byte pepsEnabled;
    private Byte withinBranchServiceArea;
    private String residency;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private Collection<Address> addressesByCustomerCode;
    private Industry industry;
    private Occupation occupation;
    private CustomerType customerType;
    private Module module;
    private Double annualTurnover;
    private String riskRating;


}
