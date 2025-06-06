package kz.readhub.book_management_service.service;

import kz.concord.concord_kafka_producer.event.EventPublisher;
import kz.readhub.book_management_service.constant.KafkaTopics;
import kz.readhub.book_management_service.dto.BookEventDto;
import kz.readhub.book_management_service.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing book events to Kafka using enterprise topic naming conventions.
 * Routes events to different topics based on event type and business context.
 * Follows CDC, Command, and Domain Event patterns for proper event streaming architecture.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisherService {

    private final EventPublisher eventPublisher;

    /**
     * Publishes book created event to CDC topic for change data capture.
     * Also publishes to domain events topic for business-level processing.
     */
    public Mono<Void> publishBookCreatedEvent(Book book) {
        Map<String, Object> metadata = Map.of(
            "operation", "create", 
            "trigger", "user_upload",
            "source_service", "book-management-service"
        );
        
        // Publish to CDC topic for data replication
        Mono<Void> cdcEvent = publishBookEvent(book, BookEventDto.EventType.I, null, 
                metadata, KafkaTopics.BOOK_CDC_EVENTS);
        
        // Publish to domain events topic for business logic
        Mono<Void> domainEvent = publishDomainEvent(book, "BOOK_PUBLISHED", metadata);
        
        return Mono.when(cdcEvent, domainEvent);
    }

    /**
     * Publishes book updated event with both current and previous book data.
     * Routes to CDC topic for change tracking and analytics for metrics.
     */
    public Mono<Void> publishBookUpdatedEvent(Book book, Book previousBook) {
        Map<String, Object> metadata = Map.of(
            "operation", "update", 
            "trigger", "user_modification",
            "source_service", "book-management-service",
            "has_previous_data", previousBook != null
        );
        
        // Publish to CDC topic
        Mono<Void> cdcEvent = publishBookEvent(book, BookEventDto.EventType.U, previousBook, 
                metadata, KafkaTopics.BOOK_CDC_EVENTS);
        
        // Publish to analytics topic for metrics
        Mono<Void> analyticsEvent = publishAnalyticsEvent(book, "book_updated", metadata);
        
        return Mono.when(cdcEvent, analyticsEvent);
    }

    /**
     * Publishes book updated event (without previous data for backward compatibility).
     */
    public Mono<Void> publishBookUpdatedEvent(Book book) {
        return publishBookUpdatedEvent(book, null);
    }

    /**
     * Publishes book deleted event to CDC and audit topics.
     */
    public Mono<Void> publishBookDeletedEvent(Book book) {
        Map<String, Object> metadata = Map.of(
            "operation", "delete", 
            "trigger", "user_deletion", 
            "deletion_type", "soft",
            "source_service", "book-management-service"
        );
        
        Mono<Void> cdcEvent = publishBookEvent(book, BookEventDto.EventType.D, null,
                metadata, KafkaTopics.BOOK_CDC_EVENTS);
        
        return Mono.when(cdcEvent);
    }

    /**
     * Publishes book status changed event.
     */
    public Mono<Void> publishBookStatusChangedEvent(Book book, Book.BookStatus previousStatus) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "status_change");
        metadata.put("previous_status", previousStatus.name());
        metadata.put("new_status", book.getStatus().name());
        
        return publishBookEvent(book, BookEventDto.EventType.U, null, metadata, KafkaTopics.BOOK_CDC_EVENTS);
    }

    /**
     * Publishes book download incremented event.
     */
    public Mono<Void> publishBookDownloadIncrementedEvent(Book book, Integer previousCount) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "download_increment");
        metadata.put("previous_count", previousCount);
        metadata.put("new_count", book.getDownloadCount());
        
        return publishBookEvent(book, BookEventDto.EventType.U, null, metadata, KafkaTopics.BOOK_CDC_EVENTS);
    }

    /**
     * Publishes book rating updated event.
     */
    public Mono<Void> publishBookRatingUpdatedEvent(Book book, Map<String, Object> ratingInfo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "rating_update");
        metadata.putAll(ratingInfo);
        
        return publishBookEvent(book, BookEventDto.EventType.U, null, metadata, KafkaTopics.BOOK_CDC_EVENTS);
    }

    /**
     * Publishes batch created event for multiple books.
     */
    public Mono<Void> publishBatchCreatedEvent(List<Book> books) {
        log.info("Publishing batch created event for {} books", books.size());
        
        Map<String, Object> metadata = Map.of(
            "operation", "batch_create",
            "batch_size", books.size(),
            "book_ids", books.stream().map(Book::getId).toList()
        );

        BookEventDto event = BookEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BookEventDto.EventType.I)
                .bookId("batch-" + UUID.randomUUID())
                .bookData(null) // No single book data for batch events
                .eventTimestamp(LocalDateTime.now())
                .metadata(metadata)
                .correlationId(generateCorrelationId())
                .build();

        // Send to analytics topic for batch operations
        CompletableFuture<Void> future = eventPublisher.publish(KafkaTopics.BOOK_ANALYTICS_EVENTS, event.getEventId(), event);
        
        return Mono.fromFuture(future)
                .doOnSuccess(unused -> log.info("Successfully published batch created event for {} books", books.size()))
                .doOnError(error -> log.error("Failed to publish batch created event", error));
    }

    /**
     * Core method to publish book events to specified topic.
     */
    private Mono<Void> publishBookEvent(Book book, BookEventDto.EventType eventType, 
                                       Book previousBook, Map<String, Object> metadata, String topic) {
        log.info("Publishing {} event for book id: {}", eventType, book.getId());

        BookEventDto event = BookEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .bookId(book.getId())
                .bookData(book)
                .previousBookData(previousBook)
                .triggeredBy(book.getUploadedBy())
                .eventTimestamp(LocalDateTime.now())
                .metadata(metadata)
                .correlationId(generateCorrelationId())
                .build();

        // Use book ID as partition key to ensure ordering per book
        CompletableFuture<Void> future = eventPublisher.publish(topic, book.getId(), event);
        
        return Mono.fromFuture(future)
                .doOnSuccess(unused -> log.info("Successfully published {} event for book: {}", eventType, book.getId()))
                .doOnError(error -> log.error("Failed to publish {} event for book: {}", eventType, book.getId(), error));
    }

    /**
     * Publishes domain event for business-level processing.
     */
    private Mono<Void> publishDomainEvent(Book book, String eventName, Map<String, Object> metadata) {
        Map<String, Object> domainMetadata = new HashMap<>(metadata);
        domainMetadata.put("domain_event_name", eventName);
        domainMetadata.put("aggregate_type", "Book");
        domainMetadata.put("aggregate_id", book.getId());
        
        BookEventDto event = BookEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BookEventDto.EventType.I) // Domain events are always inserts
                .bookId(book.getId())
                .bookData(book)
                .eventTimestamp(LocalDateTime.now())
                .metadata(domainMetadata)
                .correlationId(generateCorrelationId())
                .build();

        CompletableFuture<Void> future = eventPublisher.publish(KafkaTopics.BOOK_CDC_EVENTS, book.getId(), event);
        
        return Mono.fromFuture(future)
                .doOnSuccess(unused -> log.info("Successfully published domain event {} for book: {}", eventName, book.getId()))
                .doOnError(error -> log.error("Failed to publish domain event {} for book: {}", eventName, book.getId(), error));
    }

    /**
     * Publishes analytics event for metrics and reporting.
     */
    private Mono<Void> publishAnalyticsEvent(Book book, String metricName, Map<String, Object> metadata) {
        Map<String, Object> analyticsMetadata = new HashMap<>(metadata);
        analyticsMetadata.put("metric_name", metricName);
        analyticsMetadata.put("entity_type", "book");
        analyticsMetadata.put("timestamp", LocalDateTime.now());
        
        BookEventDto event = BookEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BookEventDto.EventType.U) // Analytics events are updates
                .bookId(book.getId())
                .bookData(book)
                .eventTimestamp(LocalDateTime.now())
                .metadata(analyticsMetadata)
                .correlationId(generateCorrelationId())
                .build();

        CompletableFuture<Void> future = eventPublisher.publish(KafkaTopics.BOOK_ANALYTICS_EVENTS, book.getId(), event);
        
        return Mono.fromFuture(future)
                .doOnSuccess(unused -> log.info("Successfully published analytics event {} for book: {}", metricName, book.getId()))
                .doOnError(error -> log.error("Failed to publish analytics event {} for book: {}", metricName, book.getId(), error));
    }

    private String generateCorrelationId() {
        return "book-mgmt-" + UUID.randomUUID().toString().substring(0, 8);
    }
}