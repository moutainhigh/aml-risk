package com.redhat.aml;

import java.util.Date;


public class ChannelUsage {
    private String channel;
    private Date date;
    private Double channelRisk;

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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getChannelRisk() {
        return channelRisk;
    }

    public void setChannelRisk(Double channelRisk) {
        this.channelRisk = channelRisk;
    }
}
