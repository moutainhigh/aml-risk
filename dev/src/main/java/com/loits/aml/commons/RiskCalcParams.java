package com.loits.aml.commons;

import lombok.Data;

@Data
public class RiskCalcParams {

  private String operation = null;
  private boolean calcCategoryRisk = true;
  private boolean calcChannelRisk= true;
  private boolean calcProductRisk = true;
  private Integer pageLimit;
  private Integer parallelCount;
  private Integer recordLimit;
  private Integer size;
  private Integer page;
  private Integer offset;
  private String calcGroup;

  public RiskCalcParams() {
  }

  @Override
  public String toString() {
    return "RiskCalcParams{" +
            "operation='" + operation + '\'' +
            ", calcCategoryRisk=" + calcCategoryRisk +
            ", calcChannelRisk=" + calcChannelRisk +
            ", calcProductRisk=" + calcProductRisk +
            ", pageLimit=" + pageLimit +
            ", parallelCount=" + parallelCount +
            ", recordLimit=" + recordLimit +
            ", size=" + size +
            ", page=" + page +
            ", offset=" + offset +
            ", calcGroup='" + calcGroup + '\'' +
            '}';
  }
}

