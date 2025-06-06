package kz.readhub.book_management_service.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base request DTO containing common request metadata and tracking information.
 * All request DTOs should extend this for consistent request handling.
 */
@Data
@SuperBuilder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseRequestDto {

    /**
     * Client-provided request ID for correlation and debugging.
     * Should be unique per request for traceability.
     */
    @Size(max = 100, message = "Request ID must not exceed 100 characters")
    protected String requestId;

    /**
     * User identifier who initiated the request.
     * Used for authorization, auditing, and business logic.
     */
    @NotBlank(message = "User ID is required")
    @Size(max = 100, message = "User ID must not exceed 100 characters")
    protected String userId;

    /**
     * Source system or client application that made the request.
     * Useful for analytics and system integration tracking.
     */
    @Size(max = 50, message = "Source must not exceed 50 characters")
    protected String source;

    /**
     * Client timestamp when the request was created.
     * Used for ordering and conflict resolution.
     */
    protected LocalDateTime clientTimestamp;

    /**
     * Additional metadata provided by the client.
     * Can include client version, feature flags, A/B test groups, etc.
     */
    protected Map<String, Object> metadata;

    /**
     * Reason or comment for the operation.
     * Useful for audit trails and business context.
     */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    protected String reason;

    // Default constructor needed for Jackson deserialization
    protected BaseRequestDto() {}
}