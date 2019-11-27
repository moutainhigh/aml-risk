package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "customer_identification")
public class CustomerIdentification {

    @Id
    @Column
    private Long id;

    @Column(name = "type")
    private String type;

    @Column(name = "value")
    private String value;

    @JsonIgnore
    @JoinColumn(name = "customer", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Customer.class)
    private Customer customer;

    public CustomerIdentification() {
    }

    @Override
    public String toString() {
        return "CustomerIdentification{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", id=" + id +
                '}';
    }
}

