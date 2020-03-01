package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {
    private String locationKey;
    private String locationCategory;
    private GeoLocation parent;
    @JsonIgnore
    private String module;
    private Integer risk;
    private Double weightage;
    private Long id;
    private Boolean riskFactorAvailability;
    private String locationName;

    public GeoLocation() {
    }

    public GeoLocation(String locationKey, String locationCategory, GeoLocation parent, String module, Integer risk, Double weightage, Long id, GeoLocation parent1, Boolean riskFactorAvailability, String locationName) {
        this.locationKey = locationKey;
        this.locationCategory = locationCategory;
        this.parent = parent;
        this.module = module;
        this.risk = risk;
        this.weightage = weightage;
        this.id = id;
        this.parent = parent1;
        this.riskFactorAvailability = riskFactorAvailability;
        this.locationName = locationName;
    }
}
