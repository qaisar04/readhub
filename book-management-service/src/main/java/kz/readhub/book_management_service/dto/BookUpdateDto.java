package kz.readhub.book_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kz.readhub.book_management_service.dto.common.BaseRequestDto;
import kz.readhub.book_management_service.model.Author;
import kz.readhub.book_management_service.model.Book;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for updating existing books.
 * All fields are optional - only non-null values will be updated.
 * Extends BaseRequestDto for request tracking and metadata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookUpdateDto extends BaseRequestDto {

    // Basic book information fields (all optional for updates)
    private String title;
    private String description;
    private List<Author> authors;
    private List<String> tags;
    private List<String> categories;
    private String language;
    private LocalDate publicationDate;
    private String coverUrl;
    private String filePath;
    private Long fileSize;
    private String isbn;
    private String publisher;
    private Integer pageCount;

    // Metrics and status fields
    @Min(value = 0, message = "Average rating must be between 0 and 5")
    @Max(value = 5, message = "Average rating must be between 0 and 5")
    private Float averageRating;

    @Min(value = 0, message = "Review count cannot be negative")
    private Integer reviewCount;

    @Min(value = 0, message = "Download count cannot be negative")
    private Integer downloadCount;

    private Book.BookStatus status;

    /**
     * Checks if any book data fields are provided for update.
     */
    public boolean hasUpdates() {
        return title != null || description != null || authors != null ||
               tags != null || categories != null || language != null ||
               publicationDate != null || coverUrl != null || filePath != null ||
               fileSize != null || isbn != null || publisher != null ||
               pageCount != null || averageRating != null || reviewCount != null ||
               downloadCount != null || status != null;
    }
}