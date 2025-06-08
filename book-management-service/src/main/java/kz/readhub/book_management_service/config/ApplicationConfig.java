package kz.readhub.book_management_service.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Main application configuration for book management service.
 * Configures beans, validation, documentation, and health checks.
 * Uses Concord libraries for MongoDB and Kafka integration.
 */
@Slf4j
@Configuration
public class ApplicationConfig {

    @Bean
    @Primary
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setAmbiguityIgnored(true);
        
        log.info("ModelMapper configured with strict matching strategy");
        return mapper;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }

    @Bean
    public HealthIndicator bookServiceHealthIndicator() {
        return () -> {
            try {
                return Health.up()
                        .withDetail("service", "book-management-service")
                        .withDetail("status", "operational")
                        .withDetail("mongodb_config", "concord-mongo-autoconfigure")
                        .withDetail("kafka_config", "concord-kafka-producer")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("service", "book-management-service")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}