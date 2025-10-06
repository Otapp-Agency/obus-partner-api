package com.obuspartners.modules.booking_management.domain.dto;

import com.obuspartners.modules.booking_management.domain.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for booking response
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for booking creation")
public class BookingResponseDto {

    @Schema(description = "Unique booking identifier", example = "01HXYZ123456789ABCDEFGHIJK")
    private String bookingUid;

    @Schema(description = "Current booking status", example = "PROCESSING")
    private BookingStatus status;

    @Schema(description = "Response message", example = "Booking received and is being processed")
    private String message;

    @Schema(description = "Booking creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    public BookingResponseDto(String bookingUid, BookingStatus status, String message) {
        this.bookingUid = bookingUid;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
