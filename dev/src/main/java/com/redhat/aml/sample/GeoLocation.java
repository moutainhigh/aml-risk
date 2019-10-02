package com.redhat.aml.sample;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

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
