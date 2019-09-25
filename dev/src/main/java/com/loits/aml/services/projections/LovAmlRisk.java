package com.loits.aml.services.projections;

import com.loits.aml.domain.*;
import org.springframework.data.rest.core.config.Projection;

import java.sql.Timestamp;
import java.util.Collection;

@Projection(types = {AmlRisk.class}, name = "lovAmlRisk")
public interface LovAmlRisk {
    public Customer getCustomer();
    public Module getModule();
    public Double getCustomerRisk();
    public CustomerRisk getCustomerRiskId();
    public Double getProductRisk();
    public ProductRisk getProductRiskId();
    public Double getChannelRisk();
    public ChannelRisk getChannelRiskId();
    public Timestamp getCreatedOn();
    public String getCreatedBy();
    public Timestamp getFromDate();
    public Timestamp getToDate();
    public String getRiskRating();
    public Double getRisk();
}