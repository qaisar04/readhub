package kz.readhub.book_management_service.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kz.readhub.book_management_service.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Common filter DTO for search and list operations.
 * Provides consistent filtering capabilities across all search operations.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterDto {

    /**
     * Filter by book title (partial match, case-insensitive).
     */
    private String title;

    /**
     * Filter by author name (partial match, case-insensitive).
     */
    private String authorName;

    /**
     * Filter by language code.
     */
    private String language;

    /**
     * Filter by publisher name.
     */
    private String publisher;

    /**
     * Filter by ISBN.
     */
    private String isbn;

    /**
     * Filter by book status.
     */
    private Book.BookStatus status;

    /**
     * Filter by tags (books containing any of these tags).
     */
    private List<String> tags;

    /**
     * Filter by categories (books containing any of these categories).
     */
    private List<String> categories;

    /**
     * Filter by publication date range - from.
     */
    private LocalDate publicationDateFrom;

    /**
     * Filter by publication date range - to.
     */
    private LocalDate publicationDateTo;

    /**
     * Filter by minimum average rating.
     */
    private Double minRating;

    /**
     * Filter by maximum average rating.
     */
    private Double maxRating;

    /**
     * Filter by minimum page count.
     */
    private Integer minPageCount;

    /**
     * Filter by maximum page count.
     */
    private Integer maxPageCount;

    /**
     * Filter by user who uploaded the book.
     */
    private String uploadedBy;

    /**
     * Full-text search query across title, description, and author names.
     */
    private String searchQuery;

    /**
     * Checks if any filters are applied.
     */
    public boolean hasFilters() {
        return title != null || authorName != null || language != null || 
               publisher != null || isbn != null || status != null ||
               (tags != null && !tags.isEmpty()) || 
               (categories != null && !categories.isEmpty()) ||
               publicationDateFrom != null || publicationDateTo != null ||
               minRating != null || maxRating != null ||
               minPageCount != null || maxPageCount != null ||
               uploadedBy != null || searchQuery != null;
    }
}