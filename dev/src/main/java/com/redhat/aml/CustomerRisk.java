package com.redhat.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRisk {
    private Long customerCode;
    private String module;
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

    public CustomerRisk(Long customerCode, String module, Boolean pepsEnabled, Boolean withinBranchServiceArea, Industry industry, Occupation occupation, CustomerType customerType, Turnover turnover, String riskRating, Double calculatedRisk, List<GeoLocation> addresses, Double geoRisk) {
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
