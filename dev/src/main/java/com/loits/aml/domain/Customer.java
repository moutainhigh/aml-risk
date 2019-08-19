package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private Integer customerCode;
    private String surname;
    private String otherNames;
    private String title;
    private String nic;
    private String oldNic;
    private String clientCategory;
    private Byte pepsEnabled;
    private Byte withinBranchServiceArea;
    private String residency;
    private Byte status;
    private String createdBy;
    private Timestamp createdOn;
    private Long version;

    private Module module;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_code", nullable = false)
    public Integer getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(Integer customerCode) {
        this.customerCode = customerCode;
    }

    @Basic
    @Column(name = "surname", nullable = true, length = 45)
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Basic
    @Column(name = "other_names", nullable = true, length = 45)
    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    @Basic
    @Column(name = "title", nullable = true, length = 45)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "nic", nullable = true, length = 45)
    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    @Basic
    @Column(name = "old_nic", nullable = true, length = 45)
    public String getOldNic() {
        return oldNic;
    }

    public void setOldNic(String oldNic) {
        this.oldNic = oldNic;
    }

    @Basic
    @Column(name = "client_category", nullable = true, length = 45)
    public String getClientCategory() {
        return clientCategory;
    }

    public void setClientCategory(String clientCategory) {
        this.clientCategory = clientCategory;
    }

    @Basic
    @Column(name = "peps_enabled", nullable = true)
    public Byte getPepsEnabled() {
        return pepsEnabled;
    }

    public void setPepsEnabled(Byte pepsEnabled) {
        this.pepsEnabled = pepsEnabled;
    }

    @Basic
    @Column(name = "within_branch_service_area", nullable = true)
    public Byte getWithinBranchServiceArea() {
        return withinBranchServiceArea;
    }

    public void setWithinBranchServiceArea(Byte withinBranchServiceArea) {
        this.withinBranchServiceArea = withinBranchServiceArea;
    }

    @Basic
    @Column(name = "residency", nullable = true, length = 45)
    public String getResidency() {
        return residency;
    }

    public void setResidency(String residency) {
        this.residency = residency;
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
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return Objects.equals(getCustomerCode(), customer.getCustomerCode()) &&
                Objects.equals(getSurname(), customer.getSurname()) &&
                Objects.equals(getOtherNames(), customer.getOtherNames()) &&
                Objects.equals(getTitle(), customer.getTitle()) &&
                Objects.equals(getNic(), customer.getNic()) &&
                Objects.equals(getOldNic(), customer.getOldNic()) &&
                Objects.equals(getClientCategory(), customer.getClientCategory()) &&
                Objects.equals(getPepsEnabled(), customer.getPepsEnabled()) &&
                Objects.equals(getWithinBranchServiceArea(), customer.getWithinBranchServiceArea()) &&
                Objects.equals(getResidency(), customer.getResidency()) &&
                Objects.equals(getStatus(), customer.getStatus()) &&
                Objects.equals(getCreatedBy(), customer.getCreatedBy()) &&
                Objects.equals(getCreatedOn(), customer.getCreatedOn()) &&
                Objects.equals(getVersion(), customer.getVersion()) &&
                Objects.equals(getModule(), customer.getModule());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCustomerCode(), getSurname(), getOtherNames(), getTitle(), getNic(), getOldNic(), getClientCategory(), getPepsEnabled(), getWithinBranchServiceArea(), getResidency(), getStatus(), getCreatedBy(), getCreatedOn(), getVersion(), getModule());
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
