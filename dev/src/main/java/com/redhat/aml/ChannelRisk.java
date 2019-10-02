package com.redhat.aml;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ChannelRisk {
    private Long customerCode;
    private String module;
    private List<ChannelUsage> channelUsage;
    private Date today;
    private Double calculatedRisk;
    private Integer channelUsageCount;

    public ChannelRisk(Long customerCode, String module, List<ChannelUsage> channelUsage, Date today) {
        this.customerCode = customerCode;
        this.module = module;
        this.channelUsage = channelUsage;
        this.today = today;
    }

    public ChannelRisk(){

    }

    public ChannelRisk(Long customerCode, String module, List<ChannelUsage> channelUsage, Date today, Double calculatedRisk, Integer channelUsageCount) {
        this.customerCode = customerCode;
        this.module = module;
        this.channelUsage = channelUsage;
        this.today = today;
        this.calculatedRisk = calculatedRisk;
        this.channelUsageCount = channelUsageCount;
    }

}
