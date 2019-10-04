package com.loits.aml.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {
    private String locationCategory;
    private String locationKey;
    private String locationName;
    private String locationDescription;
    private Double riskScore;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private GeoLocation parent;
    private Module module;

}
