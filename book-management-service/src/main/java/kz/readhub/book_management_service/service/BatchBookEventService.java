package kz.readhub.book_management_service.service;

import kz.concord.concord_kafka_producer.batch.BatchEventPublisher;
import kz.concord.concord_kafka_producer.event.EventPublisher;
import kz.readhub.book_management_service.constant.KafkaTopics;
import kz.readhub.book_management_service.dto.BookEventDto;
import kz.readhub.book_management_service.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling batch book operations and publishing them to Kafka.
 * Uses single topic approach with proper event metadata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchBookEventService {

    private final BatchEventPublisher batchEventPublisher;
    private final EventPublisher eventPublisher;

    /**
     * Publishes individual created events for each book in a batch to the main events topic.
     * Also publishes a single batch summary event to the analytics topic.
     */
    public Mono<Void> publishBatchBookCreatedEvents(List<Book> books) {
        log.info("Publishing batch book created events for {} books", books.size());
        
        // Create individual events for each book
        List<BookEventDto> individualEvents = books.stream()
                .map(book -> createIndividualBookEvent(book, BookEventDto.EventType.I, 
                    Map.of("operation", "create", "batch_operation", true)))
                .toList();
        
        // Publish individual events to main topic
        CompletableFuture<Void> batchFuture = batchEventPublisher.publishBatch(KafkaTopics.BOOK_CDC_EVENTS, individualEvents);
        
        // Publish batch summary to analytics topic
        CompletableFuture<Void> summaryFuture = publishBatchSummaryEvent(books, BookEventDto.EventType.I);
        
        // Combine both futures
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(batchFuture, summaryFuture);
        
        return Mono.fromFuture(combinedFuture)
                .doOnSuccess(unused -> log.info("Successfully published batch book created events for {} books", books.size()))
                .doOnError(error -> log.error("Failed to publish batch book created events", error));
    }

    /**
     * Publishes batch updated events for multiple books.
     */
    public Mono<Void> publishBatchBookUpdatedEvents(List<Book> books) {
        log.info("Publishing batch book updated events for {} books", books.size());
        
        List<BookEventDto> events = books.stream()
                .map(book -> createIndividualBookEvent(book, BookEventDto.EventType.U, 
                    Map.of("operation", "update", "batch_operation", true)))
                .toList();
        
        // Publish individual events to main topic
        CompletableFuture<Void> batchFuture = batchEventPublisher.publishBatch(KafkaTopics.BOOK_CDC_EVENTS, events);
        
        // Publish batch summary to analytics topic
        CompletableFuture<Void> summaryFuture = publishBatchSummaryEvent(books, BookEventDto.EventType.U);
        
        // Combine both futures
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(batchFuture, summaryFuture);
        
        return Mono.fromFuture(combinedFuture)
                .doOnSuccess(unused -> log.info("Successfully published batch book updated events for {} books", books.size()))
                .doOnError(error -> log.error("Failed to publish batch book updated events", error));
    }

    /**
     * Creates an individual book event with metadata.
     */
    private BookEventDto createIndividualBookEvent(Book book, BookEventDto.EventType eventType, 
                                                  Map<String, Object> metadata) {
        return BookEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .bookId(book.getId())
                .bookData(book)
                .triggeredBy(book.getUploadedBy())
                .eventTimestamp(LocalDateTime.now())
                .metadata(metadata)
                .correlationId(generateCorrelationId())
                .build();
    }

    /**
     * Publishes a summary event for batch operations to analytics topic.
     */
    private CompletableFuture<Void> publishBatchSummaryEvent(List<Book> books, BookEventDto.EventType batchEventType) {
        Map<String, Object> batchMetadata = Map.of(
            "operation", "batch_summary",
            "batch_size", books.size(),
            "book_ids", books.stream().map(Book::getId).toList(),
            "categories", books.stream()
                .flatMap(book -> book.getCategories() != null ? book.getCategories().stream() : null)
                .distinct().toList(),
            "languages", books.stream()
                .map(Book::getLanguage)
                .distinct().toList()
        );

        BookEventDto batchSummaryEvent = BookEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(batchEventType)
                .bookId("batch-" + UUID.randomUUID())
                .bookData(null) // No single book data for batch summary
                .eventTimestamp(LocalDateTime.now())
                .metadata(batchMetadata)
                .correlationId(generateCorrelationId())
                .build();

        return eventPublisher.publish(KafkaTopics.BOOK_ANALYTICS_EVENTS,
                batchSummaryEvent.getEventId(), batchSummaryEvent)
                .exceptionally(throwable -> {
                    log.warn("Failed to publish batch summary event, continuing with individual events", throwable);
                    return null;
                });
    }

    private String generateCorrelationId() {
        return "batch-" + UUID.randomUUID().toString().substring(0, 8);
    }
}