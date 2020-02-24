/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loits.aml.domain;

import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author lahirubandara
 */
@Data
@Entity
@Table(name = "CALC_STATUS")
public class CalcStatus extends BaseEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "TYPE")
  private String type;

  @Column(name = "S_DATE")
  private Date sDate;

  @Column(name = "E_DATE")
  private Date eDate;

  @Column(name = "M_DATE")
  private Date mDate;

  @Column(name = "JOB_ID")
  private String jobId;

  @Column(name = "CRON_STATUS")
  private String cronStatus;

  @JoinColumn(name = "MODULE")
  private String module;

  @Column(name = "GROUP")
  private String group;

  @JoinColumn(name = "TOTAL_RECORDS")
  private int totalRecords;

  @JoinColumn(name = "UPDATED_COUNT")
  private int updatedCount;

  @JoinColumn(name = "ERROR_COUNT")
  private int errorCount;

  @Lob
  @JoinColumn(name = "META")
  private String meta;

  @Column(name = "CRITICALITY")
  private String criticality;

  @Column(name = "RAISE_NOTIFICATION")
  private String raiseNotification;

  @OneToMany(mappedBy = "calcStatus", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<CalcTasks> tasks;

  public CalcStatus() {
  }
}
