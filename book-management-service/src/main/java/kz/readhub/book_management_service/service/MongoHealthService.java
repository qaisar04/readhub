package kz.readhub.book_management_service.service;

import kz.readhub.book_management_service.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB health indicator using Concord MongoDB autoconfiguration.
 * Provides detailed health information including connection status and performance metrics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MongoHealthService implements ReactiveHealthIndicator {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Health> health() {
        return checkMongoHealth()
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(this::handleHealthCheckError);
    }

    private Mono<Health> checkMongoHealth() {
        log.debug("Performing MongoDB health check using Concord MongoDB configuration");
        
        // Simple collection count check to verify MongoDB connectivity
        return reactiveMongoTemplate.estimatedCount("books")
                .timeout(Duration.ofSeconds(3))
                .map(count -> {
                    Map<String, Object> healthInfo = new HashMap<>();
                    healthInfo.put("ping", "successful");
                    healthInfo.put("database", reactiveMongoTemplate.getCollectionName(Book.class));
                    healthInfo.put("connection", "active");
                    healthInfo.put("collection_count_check", "passed");
                    healthInfo.put("estimated_book_count", count);
                    return healthInfo;
                })
                .onErrorReturn(createErrorMap())
                .map(this::buildHealthStatus)
                .defaultIfEmpty(buildErrorHealth("No response from MongoDB"));
    }

    private Map<String, Object> createErrorMap() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("ping", "failed");
        errorMap.put("connection", "error");
        errorMap.put("database", "readhub");
        errorMap.put("collection_count_check", "failed");
        errorMap.put("estimated_book_count", -1);
        return errorMap;
    }

    private Health buildHealthStatus(Map<String, Object> mongoInfo) {
        boolean isHealthy = "successful".equals(mongoInfo.get("ping"));
        
        Health.Builder healthBuilder = isHealthy ? Health.up() : Health.down();
        
        return healthBuilder
                .withDetail("database", mongoInfo.get("database"))
                .withDetail("connection", mongoInfo.get("connection"))
                .withDetail("ping", mongoInfo.get("ping"))
                .withDetail("configured_via", "concord-mongo-autoconfigure")
                .withDetail("monitoring_enabled", true)
                .build();
    }

    private Mono<Health> handleHealthCheckError(Throwable error) {
        log.warn("MongoDB health check failed: {}", error.getMessage());
        
        return Mono.just(buildErrorHealth(error.getMessage()));
    }

    private Health buildErrorHealth(String errorMessage) {
        return Health.down()
                .withDetail("error", errorMessage)
                .withDetail("database", "readhub")
                .withDetail("configured_via", "concord-mongo-autoconfigure")
                .withDetail("monitoring_enabled", true)
                .build();
    }
}