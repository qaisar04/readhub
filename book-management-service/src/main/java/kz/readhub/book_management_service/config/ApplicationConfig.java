package kz.readhub.book_management_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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

import java.util.List;
import java.util.Set;

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
        
        // Add type converter for List<String> to Set<String>
        mapper.addConverter(context -> {
            if (context.getSource() == null) {
                return null;
            }
            return Set.copyOf((List<String>) context.getSource());
        }, List.class, Set.class);
        
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
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ReadHub Book Management API")
                        .description("Reactive microservice for book CRUD operations with MongoDB and Kafka")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ReadHub Team")
                                .email("support@readhub.kz"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
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