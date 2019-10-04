package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.aml.core.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer extends BaseEntity {
    private String surname;
    private String otherNames;
    private String title;
    private String nic;
    private String oldNic;
    private String clientCategory;
    private Byte status;

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
    @Column(name = "status", nullable = true)
    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return Objects.equals(surname, customer.surname) &&
                Objects.equals(otherNames, customer.otherNames) &&
                Objects.equals(title, customer.title) &&
                Objects.equals(nic, customer.nic) &&
                Objects.equals(oldNic, customer.oldNic) &&
                Objects.equals(clientCategory, customer.clientCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surname, otherNames, title, nic, oldNic, clientCategory);
    }
}
