package com.redhat.aml.sample;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Module {
    private String code;
    private String name;
    private String type;
    private String country;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;


}
