package com.obuspartners.api.admin;

import com.obuspartners.modules.bus_core_system.domain.dto.BusCoreSystemResponseDto;
import com.obuspartners.modules.bus_core_system.domain.dto.CreateBusCoreSystemRequestDto;
import com.obuspartners.modules.bus_core_system.domain.dto.UpdateBusCoreSystemRequestDto;
import com.obuspartners.modules.bus_core_system.service.BusCoreSystemService;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing bus core systems
 * 
 * @author OBUS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/v1/bus-core-systems")
@RequiredArgsConstructor
@Slf4j
public class AdminBusCoreSystemController {

    private final BusCoreSystemService busCoreSystemService;

    /**
     * Create a new bus core system
     * 
     * @param request the creation request
     * @return the created system response
     */
    @PostMapping
    public ResponseEntity<ResponseWrapper<BusCoreSystemResponseDto>> createBusCoreSystem(
            @Valid @RequestBody CreateBusCoreSystemRequestDto request) {
        log.info("Creating new bus core system: {}", request.getName());
        BusCoreSystemResponseDto response = busCoreSystemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>(true, 201, "Bus core system created successfully", response));
    }

    /**
     * Get all bus core systems
     * 
     * @return list of all systems
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<BusCoreSystemResponseDto>>> getAllBusCoreSystems() {
        log.debug("Fetching all bus core systems");
        List<BusCoreSystemResponseDto> systems = busCoreSystemService.getAll();
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core systems retrieved successfully", systems));
    }

    /**
     * Get bus core system by ID
     * 
     * @param id the system ID
     * @return the system response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<BusCoreSystemResponseDto>> getBusCoreSystemById(@PathVariable Long id) {
        log.debug("Fetching bus core system by ID: {}", id);
        BusCoreSystemResponseDto system = busCoreSystemService.getById(id);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core system retrieved successfully", system));
    }

    /**
     * Get bus core system by UID
     * 
     * @param uid the system UID
     * @return the system response
     */
    @GetMapping("/uid/{uid}")
    public ResponseEntity<ResponseWrapper<BusCoreSystemResponseDto>> getBusCoreSystemByUid(@PathVariable String uid) {
        log.debug("Fetching bus core system by UID: {}", uid);
        BusCoreSystemResponseDto system = busCoreSystemService.getByUid(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core system retrieved successfully", system));
    }

    /**
     * Get bus core system by name
     * 
     * @param name the system name
     * @return the system response
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseWrapper<BusCoreSystemResponseDto>> getBusCoreSystemByName(@PathVariable String name) {
        log.debug("Fetching bus core system by name: {}", name);
        BusCoreSystemResponseDto system = busCoreSystemService.getByName(name);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core system retrieved successfully", system));
    }

    /**
     * Update bus core system by UID
     * 
     * @param uid the system UID
     * @param request the update request
     * @return the updated system response
     */
    @PutMapping("/{uid}")
    public ResponseEntity<ResponseWrapper<BusCoreSystemResponseDto>> updateBusCoreSystem(
            @PathVariable String uid,
            @Valid @RequestBody UpdateBusCoreSystemRequestDto request) {
        log.info("Updating bus core system with UID: {}", uid);
        BusCoreSystemResponseDto response = busCoreSystemService.update(uid, request);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core system updated successfully", response));
    }

    /**
     * Delete bus core system by UID (soft delete)
     * 
     * @param uid the system UID
     * @return no content response
     */
    @DeleteMapping("/{uid}")
    public ResponseEntity<ResponseWrapper<String>> deleteBusCoreSystem(@PathVariable String uid) {
        log.info("Deleting bus core system with UID: {}", uid);
        busCoreSystemService.delete(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core system deleted successfully", null));
    }

    /**
     * Set bus core system as default by UID
     * 
     * @param uid the system UID
     * @return the updated system response
     */
    @PutMapping("/{uid}/set-default")
    public ResponseEntity<ResponseWrapper<BusCoreSystemResponseDto>> setAsDefault(@PathVariable String uid) {
        log.info("Setting bus core system as default with UID: {}", uid);
        BusCoreSystemResponseDto response = busCoreSystemService.setAsDefault(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core system set as default successfully", response));
    }

    /**
     * Get the default bus core system
     * 
     * @return the default system response
     */
    @GetMapping("/default")
    public ResponseEntity<ResponseWrapper<BusCoreSystemResponseDto>> getDefaultBusCoreSystem() {
        log.debug("Fetching default bus core system");
        BusCoreSystemResponseDto system = busCoreSystemService.getDefaultSystem();
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Default bus core system retrieved successfully", system));
    }

    /**
     * Global exception handler for ApiException
     * 
     * @param ex the exception
     * @return error response
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseWrapper<String>> handleApiException(ApiException ex) {
        log.error("API Exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ResponseWrapper<>(false, ex.getStatusCode().value(), ex.getMessage(), null));
    }
}
