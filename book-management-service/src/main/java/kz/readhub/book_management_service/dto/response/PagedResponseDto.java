package kz.readhub.book_management_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paged response DTO for list operations.
 * Provides consistent pagination metadata across all list responses.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedResponseDto<T> {

    /**
     * List of items in the current page.
     */
    private List<T> content;

    /**
     * Current page number (0-based).
     */
    private Integer page;

    /**
     * Number of items per page.
     */
    private Integer size;

    /**
     * Total number of elements across all pages.
     */
    private Long totalElements;

    /**
     * Total number of pages.
     */
    private Integer totalPages;

    /**
     * Whether this is the first page.
     */
    private Boolean first;

    /**
     * Whether this is the last page.
     */
    private Boolean last;

    /**
     * Number of elements in the current page.
     */
    private Integer numberOfElements;

    /**
     * Whether the page is empty.
     */
    private Boolean empty;

    /**
     * Sort information.
     */
    private SortInfo sort;

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortInfo {
        private Boolean sorted;
        private String direction;
        private String property;
    }

    /**
     * Creates a paged response from Spring Data Page.
     */
    public static <T> PagedResponseDto<T> fromPage(org.springframework.data.domain.Page<T> page) {
        return PagedResponseDto.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .sort(page.getSort().isSorted() ? 
                    SortInfo.builder()
                        .sorted(true)
                        .direction(page.getSort().iterator().next().getDirection().name())
                        .property(page.getSort().iterator().next().getProperty())
                        .build() : 
                    SortInfo.builder().sorted(false).build())
                .build();
    }

    /**
     * Creates a simple paged response from list and pagination info.
     */
    public static <T> PagedResponseDto<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return PagedResponseDto.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page == totalPages - 1)
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .sort(SortInfo.builder().sorted(false).build())
                .build();
    }
}