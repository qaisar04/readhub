package kz.readhub.book_management_service.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kz.readhub.book_management_service.model.Author;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

/**
 * Base DTO containing common book fields shared across create/update operations.
 * Uses SuperBuilder pattern for inheritance with Lombok.
 */
@Data
@SuperBuilder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseBookDto {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    protected String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    protected String description;

    @Valid
    protected List<Author> authors;

    protected List<String> tags;

    protected List<String> categories;

    @Pattern(regexp = "[a-z]{2}", message = "Language must be a 2-letter ISO code")
    protected String language;

    protected LocalDate publicationDate;

    protected String coverUrl;

    protected String filePath;

    @Positive(message = "File size must be positive")
    protected Long fileSize;

    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", 
            message = "Invalid ISBN format")
    protected String isbn;

    protected String publisher;

    @Positive(message = "Page count must be positive")
    protected Integer pageCount;

    // Default constructor needed for Jackson deserialization
    protected BaseBookDto() {}
}