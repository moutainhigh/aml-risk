package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "calc_tasks")
public class CalcTasks extends BaseEntity {

  @Column(name = "date")
  private Date date;

  @Column(name = "reference")
  private String reference;

  @Column(name = "module")
  private String module;

  @Column(name = "type")
  private String type;

  @Column(name = "s_date")
  private Timestamp sDate;

  @Column(name = "e_date")
  private Timestamp eDate;

  @Column(name = "m_date")
  private Timestamp mDate;

  @Column(name = "cron_status")
  private String cronStatus;

  @Column(name = "criticality")
  private String criticality;

  @Column(name = "raise_notification")
  private String raiseNotifications;

  @JoinColumn(name = "total_records")
  private Long totalRecords;

  @JoinColumn(name = "updated_count")
  private Long updatedCount;

  @JoinColumn(name = "error_count")
  private Long errorCount;

  @Lob
  @JoinColumn(name = "meta")
  private String meta;

  @Column(name = "job_id")
  private String jobId;


  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "calc_id")
  private CalcStatus calcStatus;

}
