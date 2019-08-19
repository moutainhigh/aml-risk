package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Integer productCode;
    private String productType;
    private String productName;
    private String productDescription;
    private Double productRisk;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Module module;
    private Long version;
    private Product parent;

    private Integer parentId;

    @JsonInclude
    @Transient
    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Id
    @Column(name = "product_code", nullable = false)
    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    @Basic
    @Column(name = "product_type", nullable = true, length = 45)
    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    @Basic
    @Column(name = "product_name", nullable = true, length = 45)
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Basic
    @Column(name = "product_description", nullable = true, length = 45)
    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Basic
    @Column(name = "product_risk", nullable = true)
    public Double getProductRisk() {
        return productRisk;
    }

    public void setProductRisk(Double productRisk) {
        this.productRisk = productRisk;
    }

    @Basic
    @Column(name = "status", nullable = true)
    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
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
    @Column(name = "version", nullable = true)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(getProductCode(), product.getProductCode()) &&
                Objects.equals(getProductType(), product.getProductType()) &&
                Objects.equals(getProductName(), product.getProductName()) &&
                Objects.equals(getProductDescription(), product.getProductDescription()) &&
                Objects.equals(getProductRisk(), product.getProductRisk()) &&
                Objects.equals(getStatus(), product.getStatus()) &&
                Objects.equals(getCreatedBy(), product.getCreatedBy()) &&
                Objects.equals(getCreatedOn(), product.getCreatedOn()) &&
                Objects.equals(getModule(), product.getModule()) &&
                Objects.equals(getVersion(), product.getVersion()) &&
                Objects.equals(getParent(), product.getParent()) &&
                Objects.equals(getParentId(), product.getParentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductCode(), getProductType(), getProductName(), getProductDescription(), getProductRisk(), getStatus(), getCreatedBy(), getCreatedOn(), getModule(), getVersion(), getParent(), getParentId());
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent", referencedColumnName = "product_code")
    public Product getParent() {
        return parent;
    }

    public void setParent(Product productByParent) {
        this.parent = productByParent;
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "module", referencedColumnName = "code", nullable = false)
    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }


}
