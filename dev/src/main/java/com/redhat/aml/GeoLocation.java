package com.redhat.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {
    private String locationKey;
    private String locationCategory;
    private GeoLocation parent;
    private String module;
    private Double risk;
    private Double weightage;

    public GeoLocation() {
    }

    public GeoLocation(String locationKey, String locationCategory, GeoLocation parent, String module, Double risk, Double weightage) {
        this.locationKey = locationKey;
        this.locationCategory = locationCategory;
        this.parent = parent;
        this.module = module;
        this.risk = risk;
        this.weightage = weightage;
    }
}
