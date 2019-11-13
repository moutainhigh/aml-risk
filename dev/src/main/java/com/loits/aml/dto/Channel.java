package com.loits.aml.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Channel {
    private Long id;
    private String code;
    private String channelName;
    private String channelDescription;
    private Long version;

}
