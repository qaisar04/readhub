package kz.readhub.book_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kz.readhub.book_management_service.model.Author;
import kz.readhub.book_management_service.model.Book;
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
public class BookUpdateDto {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Valid
    private List<Author> authors;

    private List<String> tags;

    private List<String> categories;

    @Pattern(regexp = "[a-z]{2}", message = "Language must be a 2-letter ISO code")
    private String language;

    private LocalDate publicationDate;

    private String coverUrl;

    private String filePath;

    @Positive(message = "File size must be positive")
    private Long fileSize;

    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", 
            message = "Invalid ISBN format")
    private String isbn;

    private String publisher;

    @Positive(message = "Page count must be positive")
    private Integer pageCount;

    @Min(value = 0, message = "Average rating must be between 0 and 5")
    @Max(value = 5, message = "Average rating must be between 0 and 5")
    private Float averageRating;

    @Min(value = 0, message = "Review count cannot be negative")
    private Integer reviewCount;

    @Min(value = 0, message = "Download count cannot be negative")
    private Integer downloadCount;

    private Book.BookStatus status;
}