package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "CALC_TASKS")
public class CalcTasks extends BaseEntity {

  @Column(name = "S_DATE")
  private Date sDate;

  @Column(name = "E_DATE")
  private Date eDate;

  @Column(name = "M_DATE")
  private Date mDate;

  @Column(name = "CRON_STATUS")
  private String cronStatus;

  @Column(name = "CRITICALITY")
  private String criticality;

  @Column(name = "RAISE_NOTIFICATION")
  private String raiseNotifications;

  @JoinColumn(name = "TOTAL_RECORDS")
  private int totalRecords;

  @JoinColumn(name = "UPDATED_COUNT")
  private int updatedCount;

  @JoinColumn(name = "PROCESSED_COUNT")
  private int processedCount;

  @JoinColumn(name = "ERROR_COUNT")
  private int errorCount;

  @Lob
  @JoinColumn(name = "META")
  private String meta;

  @Column(name = "JOB_ID")
  private String jobId;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "CALC_ID")
  private CalcStatus calcStatus;

  @OneToMany(mappedBy = "calcTask", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<CalcLog> calcLogs;

}
