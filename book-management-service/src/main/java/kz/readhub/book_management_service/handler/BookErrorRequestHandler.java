package kz.readhub.book_management_service.handler;

import kz.readhub.book_management_service.exception.BookManagementException;
import kz.readhub.book_management_service.exception.BookNotFoundException;
import kz.readhub.book_management_service.exception.DuplicateIsbnException;
import kz.readhub.book_management_service.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BookErrorRequestHandler implements ErrorRequestHandler {

    @Override
    public Mono<ServerResponse> handleError(Throwable throwable) {
        log.error("Processing error: {}", throwable.getMessage(), throwable);
        
        HttpStatus status = determineHttpStatus(throwable);
        String message = extractErrorMessage(throwable);
        
        ErrorResponse errorResponse = buildErrorResponse(status, message);
        
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private HttpStatus determineHttpStatus(Throwable throwable) {
        if (throwable instanceof BookNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (throwable instanceof DuplicateIsbnException) {
            return HttpStatus.CONFLICT;
        }
        if (throwable instanceof BookManagementException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (throwable instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String extractErrorMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null ? message : "An error occurred";
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
    }
}