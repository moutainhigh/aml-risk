package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Occupation {
    private Long id;
    private String isoCode;
    private Double risk;
    private Boolean industryApplicable;
    private Boolean highRisk;
    private String module;
    private Double weightage;

    public Occupation() {
    }

    public Occupation(Long id, String isoCode, Double risk, Boolean industryApplicable, Boolean highRisk, String module, Double weightage) {
        this.id = id;
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
