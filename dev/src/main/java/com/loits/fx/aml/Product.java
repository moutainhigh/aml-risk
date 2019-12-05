package com.loits.fx.aml;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Long id;
    private String code;
    private String productDescription;
    private String module;
    private Date commencedDate;
    private Date terminatedDate;
    private Double interestRate;
    private Double defaultRate;
    private Double value;
    private Double productRisk;
    private Double valueRisk;
    private Double interestRateRisk;
    private Double termRisk;
    private Double calculatedRisk;
    private List<Transaction> transactions;
    private Boolean riskFactorAvailability;
    private List<ProductRates> rates;
    private String pmeta1;
    private String pmeta2;
    private String cpmeta1;
    private String cpmeta2;
    private Double period;

    public Product() {
    }

    public Product(Long id, String code, String productDescription, String module, Date commencedDate, Date terminatedDate, Double interestRate, Double defaultRate, Double value, Double productRisk, Double valueRisk, Double interestRateRisk, Double termRisk, Double calculatedRisk, List<Transaction> transactions, Boolean riskFactorAvailability, List<ProductRates> rates, String pmeta1, String pmeta2, String cpmeta1, String cpmeta2, Double period) {
        this.id = id;
        this.code = code;
        this.productDescription = productDescription;
        this.module = module;
        this.commencedDate = commencedDate;
        this.terminatedDate = terminatedDate;
        this.interestRate = interestRate;
        this.defaultRate = defaultRate;
        this.value = value;
        this.productRisk = productRisk;
        this.valueRisk = valueRisk;
        this.interestRateRisk = interestRateRisk;
        this.termRisk = termRisk;
        this.calculatedRisk = calculatedRisk;
        this.transactions = transactions;
        this.riskFactorAvailability = riskFactorAvailability;
        this.rates = rates;
        this.pmeta1 = pmeta1;
        this.pmeta2 = pmeta2;
        this.cpmeta1 = cpmeta1;
        this.cpmeta2 = cpmeta2;
        this.period = period;
    }
}