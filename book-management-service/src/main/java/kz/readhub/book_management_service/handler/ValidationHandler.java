package kz.readhub.book_management_service.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationHandler {

    private final Validator validator;

    public <T> Mono<T> validateBody(ServerRequest request, Class<T> clazz) {
        return request.bodyToMono(clazz)
                .flatMap(this::validate);
    }

    private <T> Mono<T> validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (violations.isEmpty()) {
            return Mono.just(object);
        }

        Map<String, String> errors = new HashMap<>();
        violations.forEach(violation -> 
            errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        log.error("Validation errors: {}", errors);
        return Mono.error(new IllegalArgumentException("Validation failed: " + errors));
    }

    public Mono<ServerResponse> handleValidationError(Throwable throwable) {
        log.error("Validation error: {}", throwable.getMessage());
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                    "error", "Validation Error",
                    "message", throwable.getMessage()
                ));
    }
}