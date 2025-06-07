package kz.readhub.book_management_service.mapper;

import kz.readhub.book_management_service.avro.Author;
import kz.readhub.book_management_service.avro.Book;
import kz.readhub.book_management_service.avro.BookEvent;
import kz.readhub.book_management_service.avro.EventType;
import kz.readhub.book_management_service.model.Book.BookStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AvroMapperService {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public BookEvent createBookEvent(
            kz.readhub.book_management_service.model.Book domainBook,
            EventType eventType,
            kz.readhub.book_management_service.model.Book previousBook,
            String triggeredBy,
            Map<String, String> metadata,
            String correlationId) {

        return BookEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(eventType)
                .setBookId(domainBook != null ? domainBook.getId() : "system-generated")
                .setBookData(mapDomainBookToAvro(domainBook))
                .setPreviousBookData(previousBook != null ? mapDomainBookToAvro(previousBook) : null)
                .setTriggeredBy(triggeredBy)
                .setEventTimestamp(LocalDateTime.now().format(ISO_DATETIME_FORMATTER))
                .setSource("book-management-service")
                .setServiceVersion("1.0.0")
                .setMetadata(metadata)
                .setCorrelationId(correlationId)
                .setSchemaVersion("v1")
                .build();
    }

    public Book mapDomainBookToAvro(kz.readhub.book_management_service.model.Book domainBook) {
        if (domainBook == null) {
            return null;
        }

        return Book.newBuilder()
                .setId(domainBook.getId())
                .setTitle(domainBook.getTitle())
                .setDescription(domainBook.getDescription())
                .setAuthors(mapDomainAuthorsToAvro(domainBook.getAuthors()))
                .setTags(domainBook.getTags() != null ? List.copyOf(domainBook.getTags()) : null)
                .setCategories(domainBook.getCategories() != null ? List.copyOf(domainBook.getCategories()) : null)
                .setLanguage(domainBook.getLanguage())
                .setPublicationDate(formatDate(domainBook.getPublicationDate()))
                .setUploadedBy(domainBook.getUploadedBy())
                .setCoverUrl(domainBook.getCoverUrl())
                .setAverageRating(domainBook.getAverageRating())
                .setReviewCount(domainBook.getReviewCount())
                .setDownloadCount(domainBook.getDownloadCount())
                .setFilePath(domainBook.getFilePath())
                .setFileSize(domainBook.getFileSize())
                .setIsbn(domainBook.getIsbn())
                .setPublisher(domainBook.getPublisher())
                .setPageCount(domainBook.getPageCount())
                .setStatus(mapDomainBookStatusToAvro(domainBook.getStatus()))
                .setCreatedAt(formatDateTime(domainBook.getCreatedAt()))
                .setUpdatedAt(formatDateTime(domainBook.getUpdatedAt()))
                .setVersion(domainBook.getVersion())
                .build();
    }

    public List<Author> mapDomainAuthorsToAvro(List<kz.readhub.book_management_service.model.Author> domainAuthors) {
        if (domainAuthors == null) {
            return List.of();
        }

        return domainAuthors.stream()
                .map(this::mapDomainAuthorToAvro)
                .collect(Collectors.toList());
    }

    public Author mapDomainAuthorToAvro(kz.readhub.book_management_service.model.Author domainAuthor) {
        if (domainAuthor == null) {
            return null;
        }

        return Author.newBuilder()
                .setId(domainAuthor.getId())
                .setName(domainAuthor.getName())
                .setBio(domainAuthor.getBiography()) // Domain model uses 'biography' field
                .setEmail(null) // Domain model doesn't have email field
                .setProfilePictureUrl(null) // Domain model doesn't have profile picture field
                .build();
    }

    public kz.readhub.book_management_service.avro.BookStatus mapDomainBookStatusToAvro(BookStatus domainStatus) {
        if (domainStatus == null) {
            return kz.readhub.book_management_service.avro.BookStatus.DRAFT;
        }

        return switch (domainStatus) {
            case DRAFT -> kz.readhub.book_management_service.avro.BookStatus.DRAFT;
            case ACTIVE -> kz.readhub.book_management_service.avro.BookStatus.ACTIVE;
            case INACTIVE -> kz.readhub.book_management_service.avro.BookStatus.INACTIVE;
            case ARCHIVED -> kz.readhub.book_management_service.avro.BookStatus.ARCHIVED;
            case PENDING_APPROVAL -> kz.readhub.book_management_service.avro.BookStatus.PENDING_APPROVAL;
            case DELETED -> kz.readhub.book_management_service.avro.BookStatus.DELETED;
        };
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(ISO_DATE_FORMATTER) : null;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_DATETIME_FORMATTER) : null;
    }

    public String generateCorrelationId() {
        return "book-mgmt-" + UUID.randomUUID().toString().substring(0, 8);
    }
}