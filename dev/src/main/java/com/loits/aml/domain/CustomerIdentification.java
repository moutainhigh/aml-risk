package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "CUSTOMER_IDENTIFICATION")
public class CustomerIdentification {

    @Id
    @Column
    private Long id;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "VALUE")
    private String value;

    @JsonIgnore
    @JoinColumn(name = "CUSTOMER", referencedColumnName = "ID")
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

