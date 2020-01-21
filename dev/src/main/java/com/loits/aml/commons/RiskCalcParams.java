package com.loits.aml.commons;

import lombok.Data;

@Data
public class RiskCalcParams {

  private boolean calcCategoryRisk = true;
  private boolean calcChannelRisk= true;
  private boolean calcProductRisk = true;
  private Integer pageLimit;
  private Integer skip;
  private Integer recordLimit;

  public RiskCalcParams() {
  }

  @Override
  public String toString() {
    return "RiskCalcParams{" +
            "calcCategoryRisk=" + calcCategoryRisk +
            ", calcChannelRisk=" + calcChannelRisk +
            ", calcProductRisk=" + calcProductRisk +
            ", skip=" + skip +
            ", pageLimit=" + pageLimit.intValue() +
            ", recordLimit=" + recordLimit.intValue() +
            '}';
  }
}

