package com.redhat.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
}
