package kz.readhub.book_management_service.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common pagination DTO for list requests.
 * Provides consistent pagination parameters across all list operations.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationDto {

    /**
     * Page number (0-based).
     */
    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private Integer page = 0;

    /**
     * Number of items per page.
     */
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 1000, message = "Size must not exceed 1000")
    @Builder.Default
    private Integer size = 20;

    /**
     * Sort field name.
     */
    private String sortBy;

    /**
     * Sort direction (ASC/DESC).
     */
    @Builder.Default
    private SortDirection sortDirection = SortDirection.ASC;

    public enum SortDirection {
        ASC, DESC
    }

    /**
     * Gets the offset for database queries.
     */
    public int getOffset() {
        return page * size;
    }

    /**
     * Validates that pagination parameters are within acceptable ranges.
     */
    public void validate() {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (size < 1 || size > 1000) {
            throw new IllegalArgumentException("Size must be between 1 and 1000");
        }
    }
}