package com.loits.aml.domain;

import com.loits.aml.core.BaseEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "aml_risk")
public class AmlRisk extends BaseEntity {
    private Long customer;
    private String module;
    private Double customerRisk;
    private Long customerRiskId;
    private Double productRisk;
    private Long productRiskId;
    private Double channelRisk;
    private Long channelRiskId;
    private Timestamp createdOn;
    private String createdBy;
    private String riskRating;
    private Double risk;

    @Basic
    @Column(name = "customer", nullable = false)
    public Long getCustomer() {
        return customer;
    }

    public void setCustomer(Long customer) {
        this.customer = customer;
    }

    @Basic
    @Column(name = "module", nullable = true, length = 45)
    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Basic
    @Column(name = "customer_risk", nullable = true, precision = 0)
    public Double getCustomerRisk() {
        return customerRisk;
    }

    public void setCustomerRisk(Double customerRisk) {
        this.customerRisk = customerRisk;
    }

    @Basic
    @Column(name = "customer_risk_id", nullable = false)
    public Long getCustomerRiskId() {
        return customerRiskId;
    }

    public void setCustomerRiskId(Long customerRiskId) {
        this.customerRiskId = customerRiskId;
    }

    @Basic
    @Column(name = "product_risk", nullable = true, precision = 0)
    public Double getProductRisk() {
        return productRisk;
    }

    public void setProductRisk(Double productRisk) {
        this.productRisk = productRisk;
    }

    @Basic
    @Column(name = "product_risk_id", nullable = false)
    public Long getProductRiskId() {
        return productRiskId;
    }

    public void setProductRiskId(Long productRiskId) {
        this.productRiskId = productRiskId;
    }

    @Basic
    @Column(name = "channel_risk", nullable = true, precision = 0)
    public Double getChannelRisk() {
        return channelRisk;
    }

    public void setChannelRisk(Double channelRisk) {
        this.channelRisk = channelRisk;
    }

    @Basic
    @Column(name = "channel_risk_id", nullable = false)
    public Long getChannelRiskId() {
        return channelRiskId;
    }

    public void setChannelRiskId(Long channelRiskId) {
        this.channelRiskId = channelRiskId;
    }

    @Basic
    @Column(name = "created_on", nullable = true)
    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    @Basic
    @Column(name = "created_by", nullable = true, length = 45)
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Basic
    @Column(name = "risk_rating", nullable = true, length = 45)
    public String getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(String riskRating) {
        this.riskRating = riskRating;
    }

    @Basic
    @Column(name = "risk", nullable = true, precision = 0)
    public Double getRisk() {
        return risk;
    }

    public void setRisk(Double risk) {
        this.risk = risk;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AmlRisk amlRisk = (AmlRisk) object;
        return Objects.equals(id, amlRisk.id) &&
                Objects.equals(customer, amlRisk.customer) &&
                Objects.equals(module, amlRisk.module) &&
                Objects.equals(customerRisk, amlRisk.customerRisk) &&
                Objects.equals(customerRiskId, amlRisk.customerRiskId) &&
                Objects.equals(productRisk, amlRisk.productRisk) &&
                Objects.equals(productRiskId, amlRisk.productRiskId) &&
                Objects.equals(channelRisk, amlRisk.channelRisk) &&
                Objects.equals(channelRiskId, amlRisk.channelRiskId) &&
                Objects.equals(createdOn, amlRisk.createdOn) &&
                Objects.equals(createdBy, amlRisk.createdBy) &&
                Objects.equals(riskRating, amlRisk.riskRating) &&
                Objects.equals(risk, amlRisk.risk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customer, module, customerRisk, customerRiskId, productRisk, productRiskId, channelRisk, channelRiskId, createdOn, createdBy, riskRating, risk);
    }
}
