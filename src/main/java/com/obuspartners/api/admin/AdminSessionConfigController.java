package com.obuspartners.api.admin;

import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import com.obuspartners.modules.user_and_role_management.domain.enums.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for providing configuration and reference data
 * to the Admin UI.
 *
 * <p>
 * Exposes enums and other static configuration values so the
 * frontend can populate dropdowns, filters, and selectors without
 * hardcoding them.
 * </p>
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/session-config")
@CrossOrigin(origins = "*")
@Tag(name = "Admin Session Config", description = "Provides configuration data for Admin UI")
public class AdminSessionConfigController {

    /**
     * Retrieves reference configuration values for the admin UI.
     *
     * <p>
     * Includes enums such as:
     * <ul>
     * <li>User types</li>
     * <li>User roles</li>
     * </ul>
     * </p>
     *
     * @return map of config keys with their available values
     */
    @GetMapping
    @Operation(summary = "Get configuration data", description = "Provides static configuration values (enums, types) needed by the Admin UI.")
    public ResponseEntity<ResponseWrapper<AdminConfigResponse>> getConfig() {
        log.debug("Fetching admin session configuration");
        
        AdminConfigResponse response = new AdminConfigResponse();

        // User types
        response.setUserTypes(Arrays.stream(UserType.values())
                .map(userType -> new EnumItem(userType.getName(), userType.getDisplayName(),
                        userType.getDescription()))
                .collect(Collectors.toList()));

        // User roles
        response.setUserRoles(Arrays.stream(RoleType.values())
                .map(role -> new EnumItem(role.getValue(), role.getDisplayName(),
                        role.getDescription()))
                .collect(Collectors.toList()));

        log.info("Admin session configuration fetched successfully");
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Configuration fetched successfully", response));
    }

    // -------------------------
    // Inner DTOs for response
    // -------------------------

    @lombok.Data
    public static class AdminConfigResponse {
        private List<EnumItem> userTypes;
        private List<EnumItem> userRoles;
    }

    @lombok.AllArgsConstructor
    @lombok.Data
    public static class EnumItem {
        private String value;
        private String displayName;
        private String description;
    }
}
