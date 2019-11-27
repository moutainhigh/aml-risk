package com.loits.aml.dto;

import lombok.Data;

@Data
public class CustomerRiskOutput {

    private String customerCode;

    private String module;

    private Double calculatedRisk;

    private String riskRating;
}
