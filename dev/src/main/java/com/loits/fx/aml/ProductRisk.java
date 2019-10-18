package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRisk {
    private Long id;
    private Long customerCode;
    private String module;
    private Date today;
    private List<Product> products;
    private Double calculatedRisk;
}
