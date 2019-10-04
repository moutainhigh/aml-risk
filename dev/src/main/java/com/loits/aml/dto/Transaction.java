package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction extends BaseEntity {
    private Customer customer;
    private Timestamp txnDate;
    private Product product;
    private Channel channel;
    private String txnType;
    private String txnReference;
    private String remarks;
    private String createdBy;
    private Timestamp createdOn;
    private String module;
    private Double amount;
    private String otherpartyName;
    private String sourceOfFunds;
    private String branch;
    private String facilityBranch;
    private Long otherpartyId;
    private String otherpartyRemark;

}
