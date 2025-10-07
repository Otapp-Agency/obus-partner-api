package com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BmsLgSearchBusRequestDto {
    @JsonProperty("imei")
    private String imei;
    
    @JsonProperty("lat")
    private String lat;
    
    @JsonProperty("long")
    private String longitude;
    
    @JsonProperty("ip")
    private String ip;
    
    @JsonProperty("owner_id")
    private String ownerId;
    
    @JsonProperty("auth_key")
    private String authKey;
    
    @JsonProperty("is_from")
    private String isFrom;
    
    @JsonProperty("pltfm")
    private String pltfm;
    
    @JsonProperty("lang")
    private String lang;
    
    @JsonProperty("app_ver")
    private String appVer;
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("agent_id")
    private String agentId;
    
    @JsonProperty("from_id")
    private String fromId;
    
    @JsonProperty("to_id")
    private String toId;
    
    @JsonProperty("trvl_dt")
    private String trvlDt;
}

