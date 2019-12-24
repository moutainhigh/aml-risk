package com.loits.aml.domain;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
public class Module {

    @Id
    @Basic
    @Column(name = "CODE", nullable = false, length = 45)
    private String code;

    @Basic
    @Column(name = "NAME", nullable = true, length = 60)
    private String name;

    @Basic
    @Column(name = "TYPE", nullable = true, length = 30)
    private String type;

    @Basic
    @Column(name = "COUNTRY", nullable = true, length = 45)
    private String country;

    @Basic
    @Column(name = "CREATED_BY", nullable = true, length = 45)
    private String createdBy;

    @Basic
    @Column(name = "CREATED_ON", nullable = true)
    private Timestamp createdOn;

    @ManyToOne
    @JoinColumn(name = "PARENT", referencedColumnName = "code")
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
                ", tenent='" + tenent + '\'' +
                '}';
    }
}
