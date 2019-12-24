package com.loits.aml.domain;

import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "KAFKA_ERROR_LOG")
public class KafkaErrorLog extends BaseEntity {
    @Basic
    @Column(name = "TOPIC", nullable = false, length = 45)
    private String topic;

    @Basic
    @Column(name = "DATA", nullable = true)
    private String data;

    @Basic
    @Column(name = "TYPE", nullable = true, length = 45)
    private String type;

    @Basic
    @Column(name = "SUB_TYPE", nullable = true, length = 45)
    private String subType;

    @Basic
    @Column(name = "ERROR_MESSAGE", nullable = true, length = 150)
    private String errorMessage;

    @Basic
    @Column(name = "TRACE", nullable = true, length = -1)
    private String trace;

    @Basic
    @Column(name = "TIMESTAMP", nullable = false)
    private Timestamp timestamp;

    public KafkaErrorLog() {
    }

    public KafkaErrorLog(String topic, String data, String type, String subType, String errorMessage, String trace, Timestamp timestamp) {
        this.topic = topic;
        this.data = data;
        this.type = type;
        this.subType = subType;
        this.errorMessage = errorMessage;
        this.trace = trace;
        this.timestamp = timestamp;
    }

}
