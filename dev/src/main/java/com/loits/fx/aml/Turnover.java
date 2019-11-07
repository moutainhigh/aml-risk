package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Turnover {
    private String customerType;
    private Double turnoverFrom;
    private Double turnoverTo;
    private Double risk;
    private String module;
    private Double weightage;

    public Turnover() {
    }

    public Turnover(String customerType, Double turnoverFrom, Double turnoverTo, Double risk, String module, Double weightage) {
        this.customerType = customerType;
        this.turnoverFrom = turnoverFrom;
        this.turnoverTo = turnoverTo;
        this.risk = risk;
        this.module = module;
        this.weightage = weightage;
    }


}
