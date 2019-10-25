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
@Table(name = "service_metadata")
public class ServiceMetadata implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "meta_key")
  private String metaKey;

  @Column(name = "meta_value")
  private String metaValue;

  @Column(name = "type")
  private String metaType;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_date")
  private Date createdDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "modified_date")
  private Date ModifiedDate;


}
