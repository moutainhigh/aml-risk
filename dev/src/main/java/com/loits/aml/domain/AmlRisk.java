package com.loits.aml.domain;

import com.loits.aml.core.BaseEntity;
import com.sun.org.apache.xpath.internal.operations.Mod;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "aml_risk")
public class AmlRisk extends BaseEntity {
    private Customer customer;
    private Module module;
    private Double customerRisk;
    private CustomerRisk customerRiskId;
    private Double productRisk;
    private ProductRisk productRiskId;
    private Double channelRisk;
    private ChannelRisk channelRiskId;
    private Timestamp createdOn;
    private String createdBy;
    private Timestamp fromDate;
    private Timestamp toDate;
    private String riskRating;
    private Double risk;

    @ManyToOne
    @JoinColumn(name = "customer", nullable = false)
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }


    @ManyToOne
    @JoinColumn(name = "module", nullable = true)
    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
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

    @ManyToOne
    @JoinColumn(name = "customer_risk_id", nullable = false)
    public CustomerRisk getCustomerRiskId() {
        return customerRiskId;
    }

    public void setCustomerRiskId(CustomerRisk customerRiskId) {
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

    @ManyToOne
    @JoinColumn(name = "product_risk_id", nullable = false)
    public ProductRisk getProductRiskId() {
        return productRiskId;
    }

    public void setProductRiskId(ProductRisk productRiskId) {
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

    @ManyToOne
    @JoinColumn(name = "channel_risk_id", nullable = false)
    public ChannelRisk getChannelRiskId() {
        return channelRiskId;
    }

    public void setChannelRiskId(ChannelRisk channelRiskId) {
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
    @Column(name = "from_date", nullable = true)
    public Timestamp getFromDate() {
        return fromDate;
    }

    public void setFromDate(Timestamp fromDate) {
        this.fromDate = fromDate;
    }

    @Basic
    @Column(name = "to_date", nullable = true)
    public Timestamp getToDate() {
        return toDate;
    }

    public void setToDate(Timestamp toDate) {
        this.toDate = toDate;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmlRisk)) return false;
        AmlRisk amlRisk = (AmlRisk) o;
        return Objects.equals(customer, amlRisk.customer) &&
                Objects.equals(module, amlRisk.module) &&
                Objects.equals(customerRisk, amlRisk.customerRisk) &&
                Objects.equals(customerRiskId, amlRisk.customerRiskId) &&
                Objects.equals(productRisk, amlRisk.productRisk) &&
                Objects.equals(productRiskId, amlRisk.productRiskId) &&
                Objects.equals(channelRisk, amlRisk.channelRisk) &&
                Objects.equals(channelRiskId, amlRisk.channelRiskId) &&
                Objects.equals(createdOn, amlRisk.createdOn) &&
                Objects.equals(createdBy, amlRisk.createdBy) &&
                Objects.equals(fromDate, amlRisk.fromDate) &&
                Objects.equals(toDate, amlRisk.toDate) &&
                Objects.equals(riskRating, amlRisk.riskRating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, module, customerRisk, customerRiskId, productRisk, productRiskId, channelRisk, channelRiskId, createdOn, createdBy, fromDate, toDate, riskRating);
    }
}
