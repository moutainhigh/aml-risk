package com.redhat.aml;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Long productCode;
    private String productName;
    private String module;
    private Date commencedDate;
    private Date terminatedDate;
    private Double interestRate;
    private Double value;
    private Double defaultRate;
    private Double productRisk;
    private Double valueRisk;
    private Double interestRateRisk;
    private Double termRisk;
    private Double calculatedRisk;
    private List<Transaction> transactions;
}
