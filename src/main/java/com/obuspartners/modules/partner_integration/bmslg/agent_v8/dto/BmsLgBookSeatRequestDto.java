package com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for BMSLG Book Seat request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
public class BmsLgBookSeatRequestDto {
    //== Partner Agent Info ===
    @JsonProperty("partner_agent_id")
    private Long partnerAgentId;
    @JsonProperty("partner_id")
    private Long partnerId;
    
    // === DEVICE AND LOCATION INFO ===
    @JsonProperty("imei")
    private String imei;
    
    @JsonProperty("lat")
    private String lat;
    
    @JsonProperty("long")
    private String longitude;
    
    @JsonProperty("ip")
    private String ip;
    
    // === AUTHENTICATION AND SYSTEM INFO ===
    @JsonProperty("owner_id")
    private String ownerId;
    
    @JsonProperty("auth_key")
    private String authKey;
    
    @JsonProperty("agent_id")
    private String agentId;
    
    @JsonProperty("key")
    private String key;
    
    // === PLATFORM AND APP INFO ===
    @JsonProperty("is_from")
    private String isFrom;
    
    @JsonProperty("pltfm")
    private String pltfm;
    
    @JsonProperty("lang")
    private String lang;
    
    @JsonProperty("app_ver")
    private String appVer;
    
    // === PASSENGER DATA (JSON STRING) ===
    @JsonProperty("passengers")
    private String passengers;
    
    @JsonProperty("return_passengers")
    private String returnPassengers;
    
    // === CONTACT INFO ===
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("email")
    private String email;
    
    // === PAYMENT INFO ===
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("pay_code")
    private String payCode;
    
    @JsonProperty("pay_phone")
    private String payPhone;
    
    // === TRANSACTION PASSWORD ===
    @JsonProperty("tran_pass")
    private String tranPass;
}
