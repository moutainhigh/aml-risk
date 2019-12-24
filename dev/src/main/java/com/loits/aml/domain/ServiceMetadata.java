package com.loits.aml.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * The persistent class for the category database table.
 */
@Data
@Entity
@Table(name = "SERVICE_METADATA")
public class ServiceMetadata implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "META_KEY")
  private String metaKey;

  @Column(name = "META_VALUE")
  private String metaValue;

  @Column(name = "TYPE")
  private String metaType;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_DATE")
  private Date createdDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "MODIFIED_DATE")
  private Date ModifiedDate;


}
