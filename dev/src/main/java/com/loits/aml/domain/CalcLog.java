package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "transfer_log")
public class CalcLog extends BaseEntity {

  @Column(name = "date")
  private Date date;

  @Column(name = "description")
  private String description;

  @Column(name = "reference")
  private String reference;

  @Column(name = "ref_table")
  private String refTable;

  @Column(name = "ref_key")
  private String refKey;

  @Column(name = "ref_value")
  private String refValue;

  @Column(name = "criticality")
  private String criticality;

  @Column(name = "raise_notification")
  private String raiseNotifications;

  @Lob
  @Column(name = "stacktrace")
  private String stacktrace;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "task_id")
  private CalcTasks calcTask;

}
