package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProduct{
    private Long id;
    private Double value;
    private BigDecimal period;
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
