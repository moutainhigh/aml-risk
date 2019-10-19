package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelRisk {
    private Long id;
    private Long customerCode;
    private String module;
    private List<ChannelUsage> channelUsage;
    private List<ChannelUsage> channelUsagesAfterCalc;
    private Date today;
    private Double calculatedRisk;

    public ChannelRisk(Long customerCode, String module, List<ChannelUsage> channelUsage, Date today) {
        this.customerCode = customerCode;
        this.module = module;
        this.channelUsage = channelUsage;
        this.today = today;
    }

    public ChannelRisk(){

    }

    public ChannelRisk(Long customerCode, String module, List<ChannelUsage> channelUsage, List<ChannelUsage> channelUsagesAfterCalc, Date today, Double calculatedRisk, Integer channelUsageCount) {
        this.customerCode = customerCode;
        this.module = module;
        this.channelUsage = channelUsage;
        this.channelUsagesAfterCalc = channelUsagesAfterCalc;
        this.today = today;
        this.calculatedRisk = calculatedRisk;
    }

}
