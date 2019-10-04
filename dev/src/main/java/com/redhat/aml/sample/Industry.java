package com.redhat.aml.sample;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Industry {
    private String type;
    private String isoCode;
    private String description;
    private Double riskScore;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private Collection<Customer> customers;
    private Industry parent;
    private Collection<Industry> industriesByCode;
    private Module module;
}
