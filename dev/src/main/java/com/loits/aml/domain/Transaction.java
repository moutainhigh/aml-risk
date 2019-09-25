package com.loits.aml.domain;

import com.loits.aml.core.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

public class Transaction extends BaseEntity {
    private Customer customer;
    private Timestamp txnDate;
    private Product product;
    private Channel channel;
    private String txnType;
    private String txnReference;
    private String remarks;
    private String createdBy;
    private Timestamp createdOn;
    private String module;
    private Double amount;
    private String otherpartyName;
    private String sourceOfFunds;
    private String branch;
    private String facilityBranch;
    private Long otherpartyId;
    private String otherpartyRemark;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Timestamp getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(Timestamp txnDate) {
        this.txnDate = txnDate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getTxnReference() {
        return txnReference;
    }

    public void setTxnReference(String txnReference) {
        this.txnReference = txnReference;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getOtherpartyName() {
        return otherpartyName;
    }

    public void setOtherpartyName(String otherpartyName) {
        this.otherpartyName = otherpartyName;
    }

    public String getSourceOfFunds() {
        return sourceOfFunds;
    }

    public void setSourceOfFunds(String sourceOfFunds) {
        this.sourceOfFunds = sourceOfFunds;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getFacilityBranch() {
        return facilityBranch;
    }

    public void setFacilityBranch(String facilityBranch) {
        this.facilityBranch = facilityBranch;
    }

    public Long getOtherpartyId() {
        return otherpartyId;
    }

    public void setOtherpartyId(Long otherpartyId) {
        this.otherpartyId = otherpartyId;
    }

    public String getOtherpartyRemark() {
        return otherpartyRemark;
    }

    public void setOtherpartyRemark(String otherpartyRemark) {
        this.otherpartyRemark = otherpartyRemark;
    }
}
