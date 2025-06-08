package kz.readhub.book_management_service.handler;

import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface ErrorRequestHandler {
    
    Mono<ServerResponse> handleError(Throwable throwable);
}