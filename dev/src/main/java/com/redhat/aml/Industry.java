package com.redhat.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Industry {
    private String isoCode;
    private Double risk;
    private String module;
    private Double weightage;

    public Industry() {
    }

    public Industry(String isoCode, Double risk, String module, Double weightage) {
        this.isoCode = isoCode;
        this.risk = risk;
        this.module = module;
        this.weightage = weightage;
    }

    public Industry(String isoCode) {
        this.isoCode = isoCode;
    }
}
