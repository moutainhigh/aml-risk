package com.redhat.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Occupation {
    private String isoCode;
    private Double risk;
    private Boolean industryApplicable;
    private Boolean highRisk;
    private String module;
    private Double weightage;

    public Occupation() {
    }

    public Occupation(String isoCode, Double risk, Boolean industryApplicable, Boolean highRisk, String module, Double weightage) {
        this.isoCode = isoCode;
        this.risk = risk;
        this.industryApplicable = industryApplicable;
        this.highRisk = highRisk;
        this.module = module;
        this.weightage = weightage;
    }

    public Occupation(String isoCode) {
        this.isoCode = isoCode;
    }
}
