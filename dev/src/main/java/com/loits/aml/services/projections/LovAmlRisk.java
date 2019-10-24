package com.loits.aml.services.projections;

import com.loits.aml.domain.*;
import org.springframework.data.rest.core.config.Projection;

import java.sql.Timestamp;

@Projection(types = {AmlRisk.class}, name = "lovAmlRisk")
public interface LovAmlRisk {
    public Long getCustomer();
    public String getModule();
    public Double getCustomerRisk();
    public Long getCustomerRiskId();
    public Double getProductRisk();
    public Long getProductRiskId();
    public Double getChannelRisk();
    public Long getChannelRiskId();
    public Timestamp getCreatedOn();
    public String getCreatedBy();
    public String getRiskRating();
    public Double getRisk();
    public String getRiskText();
}