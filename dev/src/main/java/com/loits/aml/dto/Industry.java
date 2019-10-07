package com.loits.aml.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Collection;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Industry {
    private Long id;
    private String type;
    private String isoCode;
    private String description;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;
    private Module module;
}
