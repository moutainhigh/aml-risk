package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleCustomer {
    private Long id;
    private Customer customer;
    private String moduleCustomerCode;
    private Module module;
}
