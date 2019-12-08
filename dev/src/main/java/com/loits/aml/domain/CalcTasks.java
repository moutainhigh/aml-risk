package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "calc_tasks")
public class CalcTasks extends BaseEntity {

  @Column(name = "s_date")
  private Date sDate;

  @Column(name = "e_date")
  private Date eDate;

  @Column(name = "m_date")
  private Date mDate;

  @Column(name = "cron_status")
  private String cronStatus;

  @Column(name = "criticality")
  private String criticality;

  @Column(name = "raise_notification")
  private String raiseNotifications;

  @JoinColumn(name = "total_records")
  private int totalRecords;

  @JoinColumn(name = "updated_count")
  private int updatedCount;

  @JoinColumn(name = "processed_count")
  private int processedCount;

  @JoinColumn(name = "error_count")
  private int errorCount;

  @Lob
  @JoinColumn(name = "meta")
  private String meta;

  @Column(name = "job_id")
  private String jobId;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "calc_id")
  private CalcStatus calcStatus;

  @OneToMany(mappedBy = "calcTask", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<CalcLog> calcLogs;

}
