package kz.readhub.book_management_service.service;

import kz.concord.concord_kafka_producer.service.ConcordKafkaProducer;
import kz.readhub.book_management_service.avro.BookEvent;
import kz.readhub.book_management_service.avro.EventType;
import kz.readhub.book_management_service.constant.KafkaTopics;
import kz.readhub.book_management_service.mapper.AvroMapperService;
import kz.readhub.book_management_service.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisherService {

    private final ConcordKafkaProducer concordKafkaProducer;
    private final AvroMapperService avroMapperService;

    /**
     * Publishes book created event to CDC topic for change data capture.
     * Also publishes to domain events topic for business-level processing.
     */
    public Mono<Void> publishBookCreatedEvent(Book book) {
        Map<String, String> metadata = Map.of(
            "operation", "create", 
            "trigger", "user_upload",
            "source_service", "book-management-service"
        );
        
        Mono<Void> cdcEvent = publishBookEvent(book, EventType.INSERT, null,
                metadata, KafkaTopics.BOOK_CDC_EVENTS);
        
        Mono<Void> domainEvent = publishEvent(book, metadata);
        
        return Mono.when(cdcEvent, domainEvent);
    }

    /**
     * Publishes book updated event with both current and previous book data.
     * Routes to CDC topic for change tracking and analytics for metrics.
     */
    public Mono<Void> publishBookUpdatedEvent(Book book, Book previousBook) {
        Map<String, String> metadata = Map.of(
            "operation", "update", 
            "trigger", "user_modification",
            "source_service", "book-management-service",
            "has_previous_data", String.valueOf(previousBook != null)
        );
        
        Mono<Void> cdcEvent = publishBookEvent(book, EventType.UPDATE, previousBook,
                metadata, KafkaTopics.BOOK_CDC_EVENTS);
        
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
        Map<String, String> metadata = Map.of(
            "operation", "delete", 
            "trigger", "user_deletion", 
            "deletion_type", "soft",
            "source_service", "book-management-service"
        );
        
        Mono<Void> cdcEvent = publishBookEvent(book, EventType.DELETE, null,
                metadata, KafkaTopics.BOOK_CDC_EVENTS);
        
        return Mono.when(cdcEvent);
    }

    /**
     * Core method to publish book events to specified topic using Concord EventPublisher.
     */
    private Mono<Void> publishBookEvent(Book book, EventType eventType, 
                                       Book previousBook, Map<String, String> metadata, String topic) {
        log.info("Publishing {} event for book id: {}", eventType, book.getId());

        BookEvent event = avroMapperService.createBookEvent(
                book, 
                eventType, 
                previousBook, 
                book.getUploadedBy(), 
                metadata, 
                avroMapperService.generateCorrelationId()
        );

        return concordKafkaProducer.send(topic, book.getId(), event)
                .doOnSuccess(result -> log.info("Successfully published {} event for book: {}", eventType, book.getId()))
                .doOnError(error -> log.error("Failed to publish {} event for book: {}", eventType, book.getId(), error));
    }

    /**
     * Publishes domain event for business-level processing.
     */
    private Mono<Void> publishEvent(Book book, Map<String, String> metadata) {
        BookEvent event = avroMapperService.createBookEvent(
                book, 
                EventType.INSERT,
                null, 
                book.getUploadedBy(),
                metadata,
                avroMapperService.generateCorrelationId()
        );

        return concordKafkaProducer.send(KafkaTopics.BOOK_CDC_EVENTS, book.getId(), event)
                .doOnSuccess(result -> log.info("Successfully published domain event for book: {}", book.getId()))
                .doOnError(error -> log.error("Failed to publish domain event for book: {}", book.getId(), error));
    }

    /**
     * Publishes analytics event for metrics and reporting.
     */
    private Mono<Void> publishAnalyticsEvent(Book book, String metricName, Map<String, String> metadata) {
        Map<String, String> analyticsMetadata = new HashMap<>(metadata);
        analyticsMetadata.put("metric_name", metricName);
        analyticsMetadata.put("entity_type", "book");
        analyticsMetadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        var event = avroMapperService.createBookEvent(
                book, 
                EventType.UPDATE,
                null, 
                book.getUploadedBy(), 
                analyticsMetadata, 
                avroMapperService.generateCorrelationId()
        );

        return concordKafkaProducer.send(KafkaTopics.BOOK_ANALYTICS_EVENTS, book.getId(), event)
                .doOnSuccess(result -> log.info("Successfully published analytics event {} for book: {}", metricName, book.getId()))
                .doOnError(error -> log.error("Failed to publish analytics event {} for book: {}", metricName, book.getId(), error));
    }
}