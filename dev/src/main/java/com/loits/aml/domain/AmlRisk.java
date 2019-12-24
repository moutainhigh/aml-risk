package com.loits.aml.domain;

import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "aml_risk")
public class AmlRisk extends BaseEntity {

    @Basic
    @Column(name = "CUSTOMER", nullable = false)
    private Long customer;

    @Basic
    @Column(name = "MODULE", nullable = true, length = 45)
    private String module;

    @Basic
    @Column(name = "CUSTOMER_RISK", nullable = true, precision = 0)
    private Double customerRisk;

    @Basic
    @Column(name = "CUSTOMER_RISK_ID", nullable = false)
    private Long customerRiskId;

    @Basic
    @Column(name = "PRODUCT_RISK", nullable = true, precision = 0)
    private Double productRisk;

    @Basic
    @Column(name = "PRODUCT_RISK_ID", nullable = false)
    private Long productRiskId;

    @Basic
    @Column(name = "CHANNEL_RISK", nullable = true, precision = 0)
    private Double channelRisk;

    @Basic
    @Column(name = "CHANNEL_RISK_ID", nullable = false)
    private Long channelRiskId;

    @Basic
    @Column(name = "CREATED_ON", nullable = true)
    private Timestamp createdOn;

    @Basic
    @Column(name = "CREATED_BY", nullable = true, length = 45)
    private String createdBy;

    @Basic
    @Column(name = "RISK_RATING", nullable = true, length = 45)
    private String riskRating;

    @Basic
    @Column(name = "RISK", nullable = true, precision = 0)
    private Double risk;

    @Basic
    @Column(name="risk_text")
    private String riskText;

    @Transient
    private String tenent;

    @Basic
    @Column (name="RISK_CALCULATION_STATUS")
    private Long riskCalculationStatus;

}
