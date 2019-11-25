package com.loits.aml.domain;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
public class Module {
    @Id
    @Basic
    @Column(name = "code", nullable = false, length = 45)
    private String code;

    @Basic
    @Column(name = "name", nullable = true, length = 60)
    private String name;

    @Basic
    @Column(name = "type", nullable = true, length = 30)
    private String type;

    @Basic
    @Column(name = "country", nullable = true, length = 45)
    private String country;

    @Basic
    @Column(name = "created_by", nullable = true, length = 45)
    private String createdBy;

    @Basic
    @Column(name = "created_on", nullable = true)
    private Timestamp createdOn;

    @Column(name = "version", nullable = true)
    private Long version;

    @ManyToOne
    @JoinColumn(name = "parent", referencedColumnName = "code")
    private Module parent;

    @Transient
    private String tenent;

    @Override
    public String toString() {
        return "Module{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", country='" + country + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdOn=" + createdOn +
                ", version=" + version +
                ", tenent='" + tenent + '\'' +
                '}';
    }
}
