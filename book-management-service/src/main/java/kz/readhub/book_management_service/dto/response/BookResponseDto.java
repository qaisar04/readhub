package kz.readhub.book_management_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kz.readhub.book_management_service.model.Author;
import kz.readhub.book_management_service.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Response DTO for book data.
 * Provides a clean view of book information for API responses.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookResponseDto {

    private String id;
    private String title;
    private String description;
    private List<Author> authors;
    private Set<String> tags;
    private Set<String> categories;
    private String language;
    private LocalDate publicationDate;
    private String uploadedBy;
    private String coverUrl;
    private Double averageRating;
    private Integer reviewCount;
    private Integer downloadCount;
    private String filePath;
    private Long fileSize;
    private String isbn;
    private String publisher;
    private Integer pageCount;
    private Book.BookStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * Creates a response DTO from a Book entity.
     */
    public static BookResponseDto fromEntity(Book book) {
        if (book == null) {
            return null;
        }

        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .authors(book.getAuthors())
                .tags(book.getTags())
                .categories(book.getCategories())
                .language(book.getLanguage())
                .publicationDate(book.getPublicationDate())
                .uploadedBy(book.getUploadedBy())
                .coverUrl(book.getCoverUrl())
                .averageRating(book.getAverageRating())
                .reviewCount(book.getReviewCount())
                .downloadCount(book.getDownloadCount())
                .filePath(book.getFilePath())
                .fileSize(book.getFileSize())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .pageCount(book.getPageCount())
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .version(book.getVersion())
                .build();
    }

    /**
     * Checks if the book is available for download.
     */
    public boolean isDownloadable() {
        return status == Book.BookStatus.ACTIVE && filePath != null && !filePath.trim().isEmpty();
    }

    /**
     * Gets a safe display title (truncated if too long).
     */
    public String getDisplayTitle() {
        if (title == null) {
            return "Untitled";
        }
        return title.length() > 100 ? title.substring(0, 97) + "..." : title;
    }
}