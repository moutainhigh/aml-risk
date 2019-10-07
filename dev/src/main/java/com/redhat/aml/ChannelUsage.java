package com.redhat.aml;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;


@Data
public class ChannelUsage {
    private Long channelId;
    private String channel;
    private Date date;
    private Double amount;
    private Double channelRisk;
    private Long txnId;
    private String txnReference;
    private String remark;
    private String createdBy;
    private Timestamp createdOn;

    public ChannelUsage() {
    }

    public ChannelUsage(String channel, Date date) {
        this.channel = channel;
        this.date = date;
    }

    public ChannelUsage(String channel, Date date, Double channelRisk) {
        this.channel = channel;
        this.date = date;
        this.channelRisk = channelRisk;
    }
}
