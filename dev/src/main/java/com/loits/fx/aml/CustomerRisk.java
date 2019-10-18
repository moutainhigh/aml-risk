package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRisk {
    private Long id;
    private Long customerCode;
    private Module module;
    private Boolean pepsEnabled;
    private Boolean withinBranchServiceArea;;
    private Industry industry;
    private Occupation occupation;
    private CustomerType customerType;
    private Turnover turnover;
    private Double calculatedRisk;
    private List<GeoLocation> addresses;
    private Double geoRisk;

    public CustomerRisk() {
    }

    public CustomerRisk(Long id, Long customerCode, Module module, Boolean pepsEnabled, Boolean withinBranchServiceArea, Industry industry, Occupation occupation, CustomerType customerType, Turnover turnover, Double calculatedRisk, List<GeoLocation> addresses, Double geoRisk) {
        this.id = id;
        this.customerCode = customerCode;
        this.module = module;
        this.pepsEnabled = pepsEnabled;
        this.withinBranchServiceArea = withinBranchServiceArea;
        this.industry = industry;
        this.occupation = occupation;
        this.customerType = customerType;
        this.turnover = turnover;
        this.calculatedRisk = calculatedRisk;
        this.addresses = addresses;
        this.geoRisk = geoRisk;
    }
}
