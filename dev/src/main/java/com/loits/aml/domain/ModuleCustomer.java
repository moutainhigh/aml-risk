package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "MODULE_CUSTOMER")
public class ModuleCustomer{

  @Id
  @Column
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "CUSTOMER", referencedColumnName = "ID")
  private Customer customer;

  @Basic
  @Column(name = "MODULE_CUSTOMER_CODE", nullable = true, length = 45)
  private String moduleCustomerCode;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "MODULE")
  private Module module;

  @Temporal(TemporalType.TIMESTAMP)
  private Date riskCalculatedOn;

  public ModuleCustomer() {
  }

  @Override
  public String toString() {
    return "ModuleCustomer{" +
            "moduleCustomerCode='" + moduleCustomerCode + '\'' +
            ", module=" + module +
            ", id=" + id +
            '}';
  }
}
