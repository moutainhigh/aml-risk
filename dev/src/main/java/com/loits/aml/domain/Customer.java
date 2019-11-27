package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import com.loits.aml.dto.AMLCustomerModule;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "customer")
public class Customer implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "risk")
    private String customerRisk;

    @Basic
    @Column(name = "risk_score")
    private Double customerRiskScore;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleCustomer> moduleCustomers;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerIdentification> customerIdentificationList;

    @Temporal(TemporalType.TIMESTAMP)
    private Date riskCalculatedOn;

    @Transient
    private String tenent;

    public Customer() {
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", calculatedRisk='" + customerRisk + '\'' +
                ", customerRiskScore=" + customerRiskScore +
                ", customerIdentificationList=" + customerIdentificationList +
                ", riskCalculatedOn=" + riskCalculatedOn +
                ", tenent='" + tenent + '\'' +
                '}';
    }
}
