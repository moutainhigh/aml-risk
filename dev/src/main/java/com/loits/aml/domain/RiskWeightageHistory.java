package com.loits.aml.domain;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "risk_weightage_history", schema = "risk_db", catalog = "")
public class RiskWeightageHistory {
    private Integer id;
    private Integer riskWeightageId;
    private String code;
    private Integer category;
    private String name;
    private Integer weightage;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private String company;
    private String module;

    @Id
    @Column(name = "id", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "risk_weightage_id", nullable = true)
    public Integer getRiskWeightageId() {
        return riskWeightageId;
    }

    public void setRiskWeightageId(Integer riskWeightageId) {
        this.riskWeightageId = riskWeightageId;
    }

    @Basic
    @Column(name = "code", nullable = true, length = 45)
    public String getCode() {
        return code;
    }

    public void setCode(String key) {
        this.code = key;
    }

    @Basic
    @Column(name = "category", nullable = true)
    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Basic
    @Column(name = "name", nullable = true, length = 45)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "weightage", nullable = true)
    public Integer getWeightage() {
        return weightage;
    }

    public void setWeightage(Integer weightage) {
        this.weightage = weightage;
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
    @Column(name = "company", nullable = true, length = 45)
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Basic
    @Column(name = "module", nullable = true, length = 45)
    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskWeightageHistory that = (RiskWeightageHistory) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(riskWeightageId, that.riskWeightageId) &&
                Objects.equals(code, that.code) &&
                Objects.equals(category, that.category) &&
                Objects.equals(name, that.name) &&
                Objects.equals(weightage, that.weightage) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdOn, that.createdOn) &&
                Objects.equals(company, that.company) &&
                Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, riskWeightageId, code, category, name, weightage, status, createdBy, createdOn, company, module);
    }
}
