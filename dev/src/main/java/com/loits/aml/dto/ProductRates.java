package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRates {

  private BigDecimal rate;
  private BigDecimal accumulatedRate;
  private String payMode;
  private BigDecimal period;
  private String status;
  private BigDecimal companyRatio;
  private BigDecimal investorRatio;
  private BigDecimal profitRate;
  private BigDecimal profitFeeRate;
  private Date date;
  private String currency;
  private BigDecimal fromAmt;
  private BigDecimal toAmt;
}
