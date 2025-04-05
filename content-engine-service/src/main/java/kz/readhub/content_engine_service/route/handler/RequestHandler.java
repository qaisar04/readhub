package kz.readhub.content_engine_service.route.handler;

import kz.readhub.content_engine_service.service.ContentEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestHandler {

    private final ContentEngineService contentEngineService;

    public Mono<ServerResponse> search(ServerRequest request) {
        return Mono.empty();
    }
}
