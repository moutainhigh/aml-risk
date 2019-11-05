package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "geo_location")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation{

  @Id
  @Basic
  @Column(name="id")
  private Long id;

  @Column(name = "location_category", nullable = true, length = 45)
  private String locationCategory;

  @Column(name = "location_key", nullable = true, length = 45)
  private String locationKey;

  @Column(name = "location_name", nullable = true, length = 45)
  private String locationName;

  @Column(name = "location_description", nullable = true, length = 45)
  private String locationDescription;


  @Column(name = "status", nullable = true)
  private Byte status;

  @ManyToOne
  @JoinColumn(name = "parent", referencedColumnName = "id")
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
