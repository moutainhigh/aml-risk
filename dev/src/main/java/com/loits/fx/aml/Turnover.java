package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Turnover {
    private String customerType;
    private Double turnover;
    private Double risk;
    private String module;
    private Double weightage;

    public Turnover() {
    }

    public Turnover(String customerType, Double turnover, Double risk, String module, Double weightage) {
        this.customerType = customerType;
        this.turnover = turnover;
        this.risk = risk;
        this.module = module;
        this.weightage = weightage;
    }

    public Turnover(String customerType, Double turnover) {
        this.customerType = customerType;
        this.turnover = turnover;
    }
}
