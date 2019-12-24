package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "CALC_LOG")
public class CalcLog extends BaseEntity {

  @Column(name = "RUN_DATE")
  private Date date;

  @Column(name = "DESCRIPTION")
  private String description;

  @Column(name = "REFERENCE")
  private String reference;

  @Column(name = "REF_TABLE")
  private String refTable;

  @Column(name = "REF_KEY")
  private String refKey;

  @Column(name = "REF_VALUE")
  private String refValue;

  @Column(name = "CRITICALITY")
  private String criticality;

  @Column(name = "RAISE_NOTIFICATION")
  private String raiseNotifications;

  @Lob
  @Column(name = "STACKTRACE")
  private String stacktrace;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "TASK_ID")
  private CalcTasks calcTask;

}
