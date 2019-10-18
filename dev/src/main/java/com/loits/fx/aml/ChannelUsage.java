package com.loits.fx.aml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelUsage {
    private Long channelId;
    private String channel;
    private String channelName;
    private String channelDescription;
    private Date date;
    private Double amount;
    private Double channelRisk;


    public ChannelUsage() {
    }

    public ChannelUsage(String channel, Date date, Double amount) {
        this.channel = channel;
        this.date = date;
        this.amount = amount;
    }

}
