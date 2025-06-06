package kz.readhub.book_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.readhub.book_management_service.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Comprehensive book event DTO for Kafka messages.
 * Contains full book information and event metadata for downstream consumers.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookEventDto {
    
    /**
     * Unique event identifier for idempotency and tracking
     */
    @NotBlank
    private String eventId;
    
    /**
     * Type of event that occurred
     */
    @NotNull
    private EventType eventType;
    
    /**
     * Book ID for easy filtering and routing
     */
    @NotBlank
    private String bookId;
    
    /**
     * Complete book information at the time of the event
     */
    @NotNull
    private Book bookData;
    
    /**
     * Previous book data for update events (optional)
     */
    private Book previousBookData;
    
    /**
     * User who triggered the event
     */
    private String triggeredBy;
    
    /**
     * Event timestamp
     */
    @NotNull
    private LocalDateTime eventTimestamp;
    
    /**
     * Source service that generated the event
     */
    @Builder.Default
    private String source = "book-management-service";
    
    /**
     * Service version for compatibility tracking
     */
    @Builder.Default
    private String serviceVersion = "1.0.0";
    
    /**
     * Additional metadata or context
     */
    private Map<String, Object> metadata;
    
    /**
     * Correlation ID for request tracing
     */
    private String correlationId;
    
    /**
     * Event schema version for evolution support
     */
    @Builder.Default
    private String schemaVersion = "v1";
    
    /**
     * Book event types using simple I/U/D pattern
     */
    public enum EventType {
        @JsonProperty("I")
        I("Insert - Book was created"),
        
        @JsonProperty("U") 
        U("Update - Book was modified"),
        
        @JsonProperty("D")
        D("Delete - Book was removed");
        
        private final String description;
        
        EventType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}