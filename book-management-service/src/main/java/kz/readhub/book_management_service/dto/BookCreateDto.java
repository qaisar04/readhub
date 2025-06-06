package kz.readhub.book_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import kz.readhub.book_management_service.dto.common.BaseBookDto;
import kz.readhub.book_management_service.dto.common.BaseRequestDto;
import kz.readhub.book_management_service.model.Author;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO for creating new books.
 * Extends BaseBookDto for common book fields and BaseRequestDto for request metadata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookCreateDto extends BaseRequestDto {

    // Required fields for book creation
    @NotBlank(message = "Title is required")
    private String title;

    @NotEmpty(message = "At least one author is required")
    private List<Author> authors;

    @NotEmpty(message = "At least one category is required")
    private List<String> categories;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "File path is required")
    private String filePath;

    // Optional fields inherited from BaseBookDto pattern
    private String description;
    private List<String> tags;
    private String coverUrl;
    private Long fileSize;
    private String isbn;
    private String publisher;
    private Integer pageCount;
    private java.time.LocalDate publicationDate;

    /**
     * Gets the uploader user ID from the base request metadata.
     */
    public String getUploadedBy() {
        return this.getUserId();
    }
}