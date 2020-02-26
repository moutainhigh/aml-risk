package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"customerCode", "module", "customerRisk", "productRisk", "channelRisk", "pepsEnabled",
        "highRiskCustomerType", "highRiskOccupation", "riskRating", "calculatedRisk"})
@Data
public class OverallRisk {
    private Long customerCode;
    private Module module;
    private Double customerRisk;
    private Double productRisk;
    private Double channelRisk;
    private Boolean pepsEnabled;
    private Boolean highRiskCustomerType;
    private Boolean highRiskOccupation;
    private String riskRating;
    private Double calculatedRisk;

    public OverallRisk(Long customerCode, Module module, Double customerRisk, Double productRisk, Double channelRisk, Boolean pepsEnabled, Boolean highRiskCustomerType, Boolean highRiskOccupation) {
        this.customerCode = customerCode;
        this.module = module;
        this.customerRisk = customerRisk;
        this.productRisk = productRisk;
        this.channelRisk = channelRisk;
        this.pepsEnabled = pepsEnabled;
        this.highRiskCustomerType = highRiskCustomerType;
        this.highRiskOccupation = highRiskOccupation;
    }

    public OverallRisk() {
    }
}
