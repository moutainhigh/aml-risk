package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Industry {
    private Long id;
    private String isoCode;
    private Double risk;
    private String module;
    private Double weightage;

    public Industry() {
    }

    public Industry(Long id, String isoCode, Double risk, String module, Double weightage) {
        this.id = id;
        this.isoCode = isoCode;
        this.risk = risk;
        this.module = module;
        this.weightage = weightage;
    }

    public Industry(String isoCode) {
        this.isoCode = isoCode;
    }
}
