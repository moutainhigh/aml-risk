package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.fx.aml.GeoLocation;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    private String addressLine1;
    private String addressLine2;
    private Integer postalCode;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private GeoLocation geoLocation;
    private String district;
    private String country;
}
