package com.loits.aml.domain;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "risk_category", schema = "risk_db", catalog = "")
public class RiskCategory {
    private Integer id;
    private String code;
    private String description;
    private Integer valueFrom;
    private Integer valueTo;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private String company;
    private String module;
    private Long version;

    @Id
    @Column(name = "id", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "code", nullable = true, length = 45)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 45)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "value_from", nullable = true)
    public Integer getValueFrom() {
        return valueFrom;
    }

    public void setValueFrom(Integer valueFrom) {
        this.valueFrom = valueFrom;
    }

    @Basic
    @Column(name = "value_to", nullable = true)
    public Integer getValueTo() {
        return valueTo;
    }

    public void setValueTo(Integer valueTo) {
        this.valueTo = valueTo;
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
        if (o == null || getClass() != o.getClass()) return false;
        RiskCategory that = (RiskCategory) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(code, that.code) &&
                Objects.equals(description, that.description) &&
                Objects.equals(valueFrom, that.valueFrom) &&
                Objects.equals(valueTo, that.valueTo) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdOn, that.createdOn) &&
                Objects.equals(company, that.company) &&
                Objects.equals(module, that.module) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, description, valueFrom, valueTo, status, createdBy, createdOn, company, module, version);
    }
}
