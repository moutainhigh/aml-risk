/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loits.aml.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author lahirubandara
 */
@Data
@Entity
@Table(name = "sync_status")
public class SyncStatus implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "id")
  private Integer id;
  @Size(max = 45)
  @Column(name = "type")
  private String type;
  @Column(name = "page")
  private Integer page;
  @Column(name = "size")
  private Integer size;
  @Column(name = "s_date")
  private Timestamp sDate;
  @Column(name = "e_date")
  private Timestamp eDate;
  @Column(name = "m_date")
  private Timestamp mDate;
  @Size(max = 45)
  @Column(name = "job_id")
  private String jobId;
  @Column(name = "cron_status")
  private String cronStatus;

  public SyncStatus() {
  }


}
