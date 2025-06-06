package kz.readhub.book_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kz.readhub.book_management_service.dto.common.BaseRequestDto;
import kz.readhub.book_management_service.dto.common.FilterDto;
import kz.readhub.book_management_service.dto.common.PaginationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for book search and listing operations.
 * Combines filtering, pagination, and request metadata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookSearchDto extends BaseRequestDto {

    /**
     * Pagination parameters for the search.
     */
    @lombok.Builder.Default
    private PaginationDto pagination = PaginationDto.builder().build();

    /**
     * Filter criteria for the search.
     */
    @lombok.Builder.Default
    private FilterDto filters = FilterDto.builder().build();

    /**
     * Whether to include soft-deleted books in results.
     * Default is false (only active books).
     */
    @lombok.Builder.Default
    private Boolean includeDeleted = false;

    /**
     * Whether to include detailed author information.
     * Default is true.
     */
    @lombok.Builder.Default
    private Boolean includeAuthors = true;

    /**
     * Whether to include book metrics (ratings, downloads).
     * Default is true.
     */
    @lombok.Builder.Default
    private Boolean includeMetrics = true;

    /**
     * Validates the search request.
     */
    public void validate() {
        if (pagination != null) {
            pagination.validate();
        }
    }
}