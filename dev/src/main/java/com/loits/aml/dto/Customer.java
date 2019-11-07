package com.loits.aml.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private Long id;
    private String name;
    private String residency;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private Collection<Address> addresses;
    private Industry industry;
    private Occupation occupation;
    private CustomerType customerType;
    private Double annualTurnoverFrom;
    private Double annualTurnoverTo;
    private String riskRating;

    private List<CustomerMeta> customerMetaList;

    private List<CustomerIdentification> customerIdentificationList;

    private AMLCustomerModule customerModule;

}
