package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "GEO_LOCATION")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation{

  @Id
  @Basic
  @Column(name="ID")
  private Long id;

  @Column(name = "LOCATION_CATEGORY", nullable = true, length = 45)
  private String locationCategory;

  @Column(name = "LOCATION_KEY", nullable = true, length = 45)
  private String locationKey;

  @Column(name = "LOCATION_NAME", nullable = true, length = 45)
  private String locationName;

  @Column(name = "LOCATION_DESCRIPTION", nullable = true, length = 45)
  private String locationDescription;


  @Column(name = "STATUS", nullable = true)
  private Byte status;

  @ManyToOne
  @JoinColumn(name = "PARENT", referencedColumnName = "id")
  private GeoLocation parent;

  @Transient
  private String tenent;

  @Override
  public String toString() {
    return "GeoLocation{" +
            "locationCategory='" + locationCategory + '\'' +
            ", locationKey='" + locationKey + '\'' +
            ", locationName='" + locationName + '\'' +
            ", locationDescription='" + locationDescription + '\'' +
            ", status=" + status +
            ", parent=" + parent +
            ", tenent='" + tenent + '\'' +
            ", id=" + id +
            '}';
  }
}
