package com.obuspartners.modules.user_and_role_management.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.obuspartners.modules.user_and_role_management.domain.entity.SystemUser;

/**
 * Response DTO for SystemUser creation with generated password
 * Contains the created SystemUser and the generated password
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSystemUserResponse {

    private SystemUser systemUser;
    private String generatedPassword;
    private String message;

    public CreateSystemUserResponse(SystemUser systemUser, String generatedPassword) {
        this.systemUser = systemUser;
        this.generatedPassword = generatedPassword;
        this.message = "SystemUser created successfully. Please save the generated password securely.";
    }
}
