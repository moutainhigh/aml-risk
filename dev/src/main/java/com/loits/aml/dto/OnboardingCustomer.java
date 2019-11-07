package com.loits.aml.dto;

import lombok.Data;

import java.util.Collection;

@Data
public class OnboardingCustomer {
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
    private Collection<Address> addressesByCustomerCode;
    private String industry;
    private String occupation;
    private String customerType;
    private String module;
    private Double annualTurnoverFrom;
    private Double annualTurnoverTo;
    private String riskRating;
}
