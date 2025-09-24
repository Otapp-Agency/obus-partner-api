package com.obuspartners.modules.partner_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for partner validation response
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerValidationDto {

    private boolean isValid;
    private List<String> errors;
    private List<String> warnings;
    
    // Field-specific validation results
    private boolean partnerCodeValid;
    private boolean emailValid;
    private boolean phoneNumberValid;
    private boolean businessRegistrationNumberValid;
    private boolean taxIdentificationNumberValid;
    private boolean contactPersonEmailValid;
    private boolean contactPersonPhoneValid;
    
    // Duplicate check results
    private boolean partnerCodeExists;
    private boolean emailExists;
    private boolean phoneNumberExists;
    private boolean businessRegistrationNumberExists;
    private boolean taxIdentificationNumberExists;
    private boolean contactPersonEmailExists;
    private boolean contactPersonPhoneExists;
}
