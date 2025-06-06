package kz.readhub.book_management_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Author value object representing book authors.
 * Immutable value object as per DDD principles.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {
    
    private String id;
    
    @NotBlank(message = "Author name is required")
    @Size(min = 1, max = 200, message = "Author name must be between 1 and 200 characters")
    private String name;
    
    @Size(max = 2000, message = "Biography cannot exceed 2000 characters")
    private String biography;
    
    @Size(max = 100, message = "Nationality cannot exceed 100 characters")
    private String nationality;
    
    private LocalDate birthDate;
    
    private LocalDate deathDate;
    
    // Business methods
    public String getDisplayName() {
        return name != null ? name.trim() : "";
    }
    
    public boolean isAlive() {
        return deathDate == null;
    }
}