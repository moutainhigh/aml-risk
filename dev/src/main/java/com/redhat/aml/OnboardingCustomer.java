package com.redhat.aml;

import lombok.Data;

import javax.validation.constraints.Null;
import java.util.Collection;
import java.util.List;

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
    private Long version;
    private Collection<Address> addressesByCustomerCode;
    private String industry;
    private String occupation;
    private String customerType;
    private String module;
    private Double annualTurnover;
    private String riskRating;
}
