package com.loits.aml.dto;

import com.loits.fx.aml.Module;
import lombok.Data;

import java.util.Collection;

@Data
public class RiskCustomer {
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
    private Long version;
    private Collection<Address> addressesByCustomerCode;
    private Long industryId;
    private Long occupationId;
    private Long customerTypeId;
    private String industry;
    private String occupation;
    private String customerType;
    private Module module;
    private Double annualTurnover;
    private String riskRating;
}
