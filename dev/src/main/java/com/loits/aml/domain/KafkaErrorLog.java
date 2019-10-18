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
@Table(name = "kafka_error_log")
public class KafkaErrorLog extends BaseEntity {
    @Basic
    @Column(name = "topic", nullable = false, length = 45)
    private String topic;

    @Basic
    @Column(name = "data", nullable = true)
    private String data;

    @Basic
    @Column(name = "type", nullable = true, length = 45)
    private String type;

    @Basic
    @Column(name = "sub_type", nullable = true, length = 45)
    private String subType;

    @Basic
    @Column(name = "error_message", nullable = true, length = 150)
    private String errorMessage;

    @Basic
    @Column(name = "trace", nullable = true, length = -1)
    private String trace;

    @Basic
    @Column(name = "timestamp", nullable = false)
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
