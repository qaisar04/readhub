package kz.readhub.book_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import kz.readhub.book_management_service.dto.common.BaseRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO for batch operations on books.
 * Supports batch create, update, and delete operations with request tracking.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookBatchDto extends BaseRequestDto {

    /**
     * Batch operation type.
     */
    public enum BatchOperation {
        CREATE, UPDATE, DELETE, STATUS_CHANGE
    }

    /**
     * Type of batch operation to perform.
     */
    private BatchOperation operation;

    /**
     * List of books for creation (when operation = CREATE).
     */
    @Valid
    private List<BookCreateDto> booksToCreate;

    /**
     * List of book updates (when operation = UPDATE).
     */
    @Valid
    private List<BookBatchUpdateItem> booksToUpdate;

    /**
     * List of book IDs for deletion (when operation = DELETE).
     */
    private List<String> bookIdsToDelete;

    /**
     * List of book status changes (when operation = STATUS_CHANGE).
     */
    @Valid
    private List<BookBatchStatusChange> statusChanges;

    /**
     * Whether to continue processing if some items fail.
     * Default is true (fail-fast = false).
     */
    @lombok.Builder.Default
    private Boolean continueOnError = true;

    /**
     * Maximum number of items to process in this batch.
     */
    @lombok.Builder.Default
    private Integer maxBatchSize = 100;

    /**
     * Individual item for batch update operations.
     */
    @Data
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookBatchUpdateItem {
        
        @NotEmpty(message = "Book ID is required")
        private String bookId;
        
        @Valid
        private BookUpdateDto updates;
    }

    /**
     * Individual item for batch status change operations.
     */
    @Data
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookBatchStatusChange {
        
        @NotEmpty(message = "Book ID is required")
        private String bookId;
        
        private kz.readhub.book_management_service.model.Book.BookStatus newStatus;
        
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        private String reason;
    }

    /**
     * Gets the total number of items in this batch.
     */
    public int getTotalItems() {
        return switch (operation) {
            case CREATE -> booksToCreate != null ? booksToCreate.size() : 0;
            case UPDATE -> booksToUpdate != null ? booksToUpdate.size() : 0;
            case DELETE -> bookIdsToDelete != null ? bookIdsToDelete.size() : 0;
            case STATUS_CHANGE -> statusChanges != null ? statusChanges.size() : 0;
        };
    }

    /**
     * Validates the batch request.
     */
    public void validate() {
        if (operation == null) {
            throw new IllegalArgumentException("Batch operation type is required");
        }

        int totalItems = getTotalItems();
        if (totalItems == 0) {
            throw new IllegalArgumentException("At least one item must be provided for batch operation");
        }

        if (totalItems > maxBatchSize) {
            throw new IllegalArgumentException("Batch size exceeds maximum allowed: " + maxBatchSize);
        }
    }
}