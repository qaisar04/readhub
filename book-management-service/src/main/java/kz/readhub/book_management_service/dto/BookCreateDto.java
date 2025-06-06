package kz.readhub.book_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kz.readhub.book_management_service.model.Author;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookCreateDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotEmpty(message = "At least one author is required")
    @Valid
    private List<Author> authors;

    private List<String> tags;

    @NotEmpty(message = "At least one category is required")
    private List<String> categories;

    @NotBlank(message = "Language is required")
    @Pattern(regexp = "[a-z]{2}", message = "Language must be a 2-letter ISO code")
    private String language;

    private LocalDate publicationDate;

    @NotBlank(message = "Uploaded by is required")
    private String uploadedBy;

    private String coverUrl;

    @NotBlank(message = "File path is required")
    private String filePath;

    @Positive(message = "File size must be positive")
    private Long fileSize;

    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", 
            message = "Invalid ISBN format")
    private String isbn;

    private String publisher;

    @Positive(message = "Page count must be positive")
    private Integer pageCount;
}