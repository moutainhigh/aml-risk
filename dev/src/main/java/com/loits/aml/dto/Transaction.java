package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction extends BaseEntity {
    private CustomerProduct customerProduct;
    private Timestamp txnDate;
    private Channel channel;
    private String txnType;
    private String txnMode;
    private String txnReference;
    private String remarks;
    private String createdBy;
    private Timestamp createdOn;
    private BigDecimal txnAmount;
    private String otherpartyName;
    private String sourceOfFunds;
    private String txnBranch;
    private String facilityBranch;
    private String otherpartyId;
    private String otherpartyRemark;

}
