package com.redhat.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerType {
    private String customerType;
    private Double risk;
    private Boolean occupationApplicable;
    private Boolean highRisk;
    private String module;
    private Double weightage;

    public CustomerType() {
    }

    public CustomerType(String customerType, Double risk, Boolean occupationApplicable, Boolean highRisk, String module, Double weightage) {
        this.customerType = customerType;
        this.risk = risk;
        this.occupationApplicable = occupationApplicable;
        this.highRisk = highRisk;
        this.module = module;
        this.weightage = weightage;
    }

    public CustomerType(String customerType) {
        this.customerType = customerType;
    }
}
