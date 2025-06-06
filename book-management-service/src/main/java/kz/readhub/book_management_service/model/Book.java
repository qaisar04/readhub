package kz.readhub.book_management_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Book domain entity representing a book in the ReadHub system.
 * Follows DDD principles with rich domain model and validation.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "books")
@CompoundIndex(def = "{'isbn': 1, 'status': 1}", unique = true)
@CompoundIndex(def = "{'title': 'text', 'description': 'text', 'authors.name': 'text'}")
public class Book {
    
    @Id
    private String id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    @Indexed
    @Field("title")
    private String title;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    @Field("description")
    private String description;
    
    @NotEmpty(message = "At least one author is required")
    @Field("authors")
    private List<Author> authors;
    
    @Field("tags")
    private Set<String> tags;
    
    @Field("categories")
    private Set<String> categories;
    
    @NotBlank(message = "Language is required")
    @Pattern(regexp = "[a-z]{2}", message = "Language must be a valid ISO 639-1 code")
    @Indexed
    @Field("language")
    private String language;
    
    @Field("publication_date")
    private LocalDate publicationDate;
    
    @NotBlank(message = "Uploaded by is required")
    @Field("uploaded_by")
    private String uploadedBy;
    
    @Field("cover_url")
    private String coverUrl;
    
    @Min(value = 0, message = "Average rating cannot be negative")
    @Max(value = 5, message = "Average rating cannot exceed 5")
    @Field("average_rating")
    @Builder.Default
    private Double averageRating = 0.0;
    
    @Min(value = 0, message = "Review count cannot be negative")
    @Field("review_count")
    @Builder.Default
    private Integer reviewCount = 0;
    
    @Min(value = 0, message = "Download count cannot be negative")
    @Field("download_count")
    @Builder.Default
    private Integer downloadCount = 0;
    
    @Field("file_path")
    private String filePath;
    
    @Min(value = 0, message = "File size cannot be negative")
    @Field("file_size")
    private Long fileSize;
    
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", 
               message = "Invalid ISBN format")
    @Indexed(unique = true, sparse = true)
    @Field("isbn")
    private String isbn;
    
    @Field("publisher")
    private String publisher;
    
    @Min(value = 1, message = "Page count must be at least 1")
    @Field("page_count")
    private Integer pageCount;
    
    @NotNull(message = "Status is required")
    @Field("status")
    @Builder.Default
    private BookStatus status = BookStatus.DRAFT;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Field("version")
    private Long version;
    
    /**
     * Book status enum following state machine pattern
     */
    @Getter
    public enum BookStatus {
        DRAFT("Draft - Under review"),
        ACTIVE("Active - Available for reading"),
        INACTIVE("Inactive - Temporarily unavailable"),
        ARCHIVED("Archived - Permanently removed"),
        PENDING_APPROVAL("Pending Approval"),
        DELETED("Deleted - Marked for deletion");
        
        private final String description;
        
        BookStatus(String description) {
            this.description = description;
        }

    }
    
    public void incrementDownloadCount() {
        this.downloadCount = this.downloadCount + 1;
    }
    
    public void updateRating(Double newRating, int newReviewCount) {
        this.averageRating = newRating;
        this.reviewCount = newReviewCount;
    }
    
    public boolean isPublished() {
        return status == BookStatus.ACTIVE;
    }
    
    public boolean canBeDownloaded() {
        return status == BookStatus.ACTIVE && filePath != null;
    }
}