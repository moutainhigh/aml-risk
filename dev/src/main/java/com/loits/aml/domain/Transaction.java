package com.loits.aml.domain;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
public class Transaction {
    private Integer customer;
    private Integer txnId;
    private Timestamp txnDate;
    private Integer product;
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
    private Integer otherpartyId;
    private String otherpartyRemark;

    @Basic
    @Column(name = "customer", nullable = true)
    public Integer getCustomer() {
        return customer;
    }

    public void setCustomer(Integer customer) {
        this.customer = customer;
    }

    @Basic
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txn_id", nullable = false)
    public Integer getTxnId() {
        return txnId;
    }

    public void setTxnId(Integer txnId) {
        this.txnId = txnId;
    }

    @Basic
    @Column(name = "txn_date", nullable = true)
    public Timestamp getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(Timestamp txnDate) {
        this.txnDate = txnDate;
    }

    @Basic
    @Column(name = "product", nullable = true)
    public Integer getProduct() {
        return product;
    }

    public void setProduct(Integer product) {
        this.product = product;
    }

    @Basic
    @Column(name = "txn_type", nullable = true, length = 45)
    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    @Basic
    @Column(name = "txn_reference", nullable = true, length = 45)
    public String getTxnReference() {
        return txnReference;
    }

    public void setTxnReference(String txnReference) {
        this.txnReference = txnReference;
    }

    @Basic
    @Column(name = "remarks", nullable = true, length = 45)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
    @Column(name = "created_on", nullable = true)
    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    @Basic
    @Column(name = "module", nullable = true, length = 10)
    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Basic
    @Column(name = "amount", nullable = true, precision = 0)
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(customer, that.customer) &&
                Objects.equals(txnId, that.txnId) &&
                Objects.equals(txnDate, that.txnDate) &&
                Objects.equals(product, that.product) &&
                Objects.equals(txnType, that.txnType) &&
                Objects.equals(txnReference, that.txnReference) &&
                Objects.equals(remarks, that.remarks) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdOn, that.createdOn) &&
                Objects.equals(module, that.module) &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, txnId, txnDate, product, txnType, txnReference, remarks, createdBy, createdOn, module, amount);
    }

    @Basic
    @Column(name = "otherparty_name", nullable = true, length = 45)
    public String getOtherpartyName() {
        return otherpartyName;
    }

    public void setOtherpartyName(String otherpartyName) {
        this.otherpartyName = otherpartyName;
    }

    @Basic
    @Column(name = "source_of_funds", nullable = true, length = 45)
    public String getSourceOfFunds() {
        return sourceOfFunds;
    }

    public void setSourceOfFunds(String sourceOfFunds) {
        this.sourceOfFunds = sourceOfFunds;
    }

    @Basic
    @Column(name = "branch", nullable = true, length = 45)
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Basic
    @Column(name = "facility_branch", nullable = true, length = 45)
    public String getFacilityBranch() {
        return facilityBranch;
    }

    public void setFacilityBranch(String facilityBranch) {
        this.facilityBranch = facilityBranch;
    }

    @Basic
    @Column(name = "otherparty_id", nullable = true)
    public Integer getOtherpartyId() {
        return otherpartyId;
    }

    public void setOtherpartyId(Integer otherpartyId) {
        this.otherpartyId = otherpartyId;
    }

    @Basic
    @Column(name = "otherparty_remark", nullable = true, length = 45)
    public String getOtherpartyRemark() {
        return otherpartyRemark;
    }

    public void setOtherpartyRemark(String otherpartyRemark) {
        this.otherpartyRemark = otherpartyRemark;
    }
}
