package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Integer productCode;
    private String productType;
    private String code;
    private String productName;
    private String productDescription;
    private Byte status;
    private Double defaultRate;
    private Module module;
}
