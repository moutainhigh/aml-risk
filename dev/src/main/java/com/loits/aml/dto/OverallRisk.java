package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.hibernate.annotations.NotFound;

@JsonPropertyOrder({"customerCode", "module", "customerRisk", "productRisk", "channelRisk", "pepsEnabled",
        "highRiskCustomerType", "highRiskOccupation", "riskRating", "calculatedRisk"})
@Data
public class OverallRisk {
    private Long customerCode;
    private com.loits.fx.aml.Module module;
    private Double customerRisk;
    private Double productRisk;
    private Double channelRisk;
    private Boolean pepsEnabled;
    private Boolean highRiskCustomerType;
    private Boolean highRiskOccupation;
    private String riskRating;
    private Double calculatedRisk;

    public OverallRisk() {
    }
}
