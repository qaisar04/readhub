package kz.readhub.content_engine_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDocument {
    private String id;
    private String title;
    private String description;
    private List<Author> authors;
    private List<String> tags;
    private List<String> categories;
    private String language;
    private LocalDate publicationDate;
    private LocalDateTime uploadDate;
    private String uploadedBy;
    private String coverUrl;
    private float averageRating;
    private int reviewCount;
    private int downloadCount;
    private float[] embedding;
}
