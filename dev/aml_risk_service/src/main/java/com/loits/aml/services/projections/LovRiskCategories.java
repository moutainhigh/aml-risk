package com.loits.aml.services.projections;

import com.loits.aml.domain.RiskCategory;
import org.springframework.data.rest.core.config.Projection;

import java.sql.Timestamp;

@Projection(types = {RiskCategory.class}, name = "lovRiskCategories")
public interface LovRiskCategories {

    public Integer getId();

    public String getCode();

    public String getDescription();

    public Integer getFrom();

    public Integer getTo();

    public Byte getStatus();

    public String getCreatedBy();

    public Timestamp getCreatedOn();

    public String getCompany();

    public String getModule();

    public Long getVersion();
}
