/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loits.aml.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author lahirubandara
 */
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
  @JoinColumn(name = "module")
  private String module;

  public SyncStatus() {
  }

  public Timestamp getEDate() {
    return eDate;
  }

  public void setEDate(Timestamp eDate) {
    this.eDate = eDate;
  }

  public SyncStatus(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Timestamp getSDate() {
    return sDate;
  }

  public void setSDate(Timestamp sDate) {
    this.sDate = sDate;
  }

  public Timestamp getMDate() {
    return mDate;
  }

  public void setMDate(Timestamp mDate) {
    this.mDate = mDate;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getCronStatus() {
    return cronStatus;
  }

  public void setCronStatus(String cronStatus) {
    this.cronStatus = cronStatus;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof SyncStatus)) {
      return false;
    }
    SyncStatus other = (SyncStatus) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "com.mycompany.mavenproject1.client.shared.SyncStatus[ id=" + id + " ]";
  }

}
