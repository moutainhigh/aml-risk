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
@Table(name = "calc_status")
public class CalcStatus extends BaseEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "type")
  private String type;

  @Column(name = "s_date")
  private Date sDate;

  @Column(name = "e_date")
  private Date eDate;

  @Column(name = "m_date")
  private Date mDate;

  @Column(name = "job_id")
  private String jobId;

  @Column(name = "cron_status")
  private String cronStatus;

  @JoinColumn(name = "module")
  private String module;

  @JoinColumn(name = "total_records")
  private int totalRecords;

  @JoinColumn(name = "updated_count")
  private int updatedCount;

  @JoinColumn(name = "error_count")
  private int errorCount;

  @Lob
  @JoinColumn(name = "meta")
  private String meta;

  @Column(name = "criticality")
  private String criticality;

  @Column(name = "raise_notification")
  private String raiseNotification;

  @OneToMany(mappedBy = "calcStatus", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<CalcTasks> tasks;

  public CalcStatus() {
  }
}
