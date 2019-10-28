package com.loits.aml.dto;

import lombok.Data;

@Data
public class AMLCustomerModule {
  private String moduleCustomerCode;
  private String module;

  public AMLCustomerModule() {
  }

  public String getModuleCustomerCode() {
    return moduleCustomerCode;
  }

  public void setModuleCustomerCode(String moduleCustomerCode) {
    this.moduleCustomerCode = moduleCustomerCode;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }
}
