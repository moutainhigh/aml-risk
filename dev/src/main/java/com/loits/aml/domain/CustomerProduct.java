package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProduct{
    private Long id;
    private Double value;
    private Timestamp commenceDate;
    private Timestamp terminateDate;
    private Double rate;
    private String createdBy;
    private Timestamp createdOn;
    private Customer customer;
    private Product product;
    private Long version;
    private Byte status;

    private Collection<Transaction> transactions;


}
