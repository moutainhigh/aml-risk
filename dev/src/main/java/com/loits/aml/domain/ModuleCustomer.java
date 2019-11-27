package com.loits.aml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loits.aml.core.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "module_customer")
public class ModuleCustomer{

  @Id
  @Column
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "customer", referencedColumnName = "id")
  private Customer customer;

  @Basic
  @Column(name = "module_customer_code", nullable = true, length = 45)
  private String moduleCustomerCode;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "module")
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
