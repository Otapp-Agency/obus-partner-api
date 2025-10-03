package com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BmsLgProcessSeatRequestDto {
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
    
    @JsonProperty("sub_id")
    private String subId;
    
    @JsonProperty("tdi_id")
    private String tdiId;
    
    @JsonProperty("lb_id")
    private String lbId;
    
    @JsonProperty("pbi_id")
    private String pbiId;
    
    @JsonProperty("asi_id")
    private String asiId;
    
    @JsonProperty("seat_id")
    private String seatId;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("seat_type_id")
    private String seatTypeId;
    
    @JsonProperty("ukey")
    private String ukey;
    
    @JsonProperty("apbi_id")
    private String apbiId;
}
