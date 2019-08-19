package com.loits.aml.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
    private Long amount;

    @Basic
    @Column(name = "customer", nullable = true)
    public Integer getCustomer() {
        return customer;
    }

    public void setCustomer(Integer customer) {
        this.customer = customer;
    }

    @Id
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
    @Column(name = "module", nullable = true, length = 45)
    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Basic
    @Column(name = "amount", nullable = true)
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
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
}
