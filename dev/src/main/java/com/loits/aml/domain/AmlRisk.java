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
    @Column(name = "customer", nullable = false)
    private Long customer;

    @Basic
    @Column(name = "module", nullable = true, length = 45)
    private String module;

    @Basic
    @Column(name = "customer_risk", nullable = true, precision = 0)
    private Double customerRisk;

    @Basic
    @Column(name = "customer_risk_id", nullable = false)
    private Long customerRiskId;

    @Basic
    @Column(name = "product_risk", nullable = true, precision = 0)
    private Double productRisk;

    @Basic
    @Column(name = "product_risk_id", nullable = false)
    private Long productRiskId;

    @Basic
    @Column(name = "channel_risk", nullable = true, precision = 0)
    private Double channelRisk;

    @Basic
    @Column(name = "channel_risk_id", nullable = false)
    private Long channelRiskId;

    @Basic
    @Column(name = "created_on", nullable = true)
    private Timestamp createdOn;

    @Basic
    @Column(name = "created_by", nullable = true, length = 45)
    private String createdBy;

    @Basic
    @Column(name = "risk_rating", nullable = true, length = 45)
    private String riskRating;

    @Basic
    @Column(name = "risk", nullable = true, precision = 0)
    private Double risk;

    @Basic
    @Column(name="risk_text")
    private String riskText;

    @Transient
    private String tenent;

}
