package com.loits.aml.domain;

import com.loits.aml.core.BaseEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "product_risk")
public class ProductRisk extends BaseEntity {
    private Long customer;
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
        if (o == null || getClass() != o.getClass()) return false;
        ProductRisk that = (ProductRisk) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(customer, that.customer) &&
                Objects.equals(risk, that.risk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customer, risk);
    }
}
