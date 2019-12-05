package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRates {
    private Double rate;
    private Double accumulatedRate;
    private String payMode;
    private Double period;
    private String status;
    private Double companyRatio;
    private Double investorRatio;
    private Double profitRate;
    private Double profitFeeRate;
    private Date date;
    private String currency;
    private Double fromAmt;
    private Double toAmt;
}
