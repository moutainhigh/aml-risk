package com.loits.aml.services.projections;

import com.loits.aml.domain.RiskCategory;
import com.loits.aml.domain.RiskWeightage;
import org.springframework.data.rest.core.config.Projection;

import java.sql.Timestamp;

@Projection(types = {RiskWeightage.class}, name = "lovRiskWeightages")
public interface LovRiskWeightages {

    public Integer getId();

    public String getKey();

    public String getName();

    public Integer getWeightage();

    public Byte getStatus();

    public String getCreatedBy();

    public Timestamp getCreatedOn();

    public String getCompany();

    public String getModule();

    public Long getVersion();

    public RiskCategory getRiskCategoryByCategory();

}
