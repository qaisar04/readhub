package kz.readhub.book_management_service.handler;

import kz.readhub.book_management_service.dto.BookCreateDto;
import kz.readhub.book_management_service.dto.BookUpdateDto;
import kz.readhub.book_management_service.model.Book;
import kz.readhub.book_management_service.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookHandler {

    private final BookService bookService;
    private final ValidationHandler validationHandler;
    private final ErrorRequestHandler errorRequestHandler;

    public Mono<ServerResponse> createBook(ServerRequest request) {
        return validationHandler.validateBody(request, BookCreateDto.class)
                .flatMap(bookService::createBook)
                .flatMap(book -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(book))
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> getBookById(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: Getting book by id: {}", id);
        
        return bookService.getBookById(id)
                .flatMap(book -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(book))
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> getAllBooks(ServerRequest request) {
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(20);
        String sortBy = request.queryParam("sortBy").orElse("createdAt");
        String sortDirection = request.queryParam("sortDirection").orElse("desc");
        
        log.info("Handler: Getting all books - page: {}, size: {}", page, size);
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookService.getAllBooks(page, size, sortBy, sortDirection), Book.class)
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> searchBooks(ServerRequest request) {
        String query = request.queryParam("query").orElse("");
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(20);
        
        log.info("Handler: Searching books with query: {}", query);
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookService.searchBooks(query, page, size), Book.class)
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> getBooksByCategory(ServerRequest request) {
        String categoriesParam = request.queryParam("categories").orElse("");
        String[] categories = categoriesParam.split(",");
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(20);
        
        log.info("Handler: Getting books by categories: {}", String.join(",", categories));
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookService.getBooksByCategory(categories, page, size), Book.class)
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> getBooksByLanguage(ServerRequest request) {
        String language = request.queryParam("language").orElse("");
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(20);
        
        log.info("Handler: Getting books by language: {}", language);
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookService.getBooksByLanguage(language, page, size), Book.class)
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> getBooksByUploadedBy(ServerRequest request) {
        String uploadedBy = request.queryParam("uploadedBy").orElse("");
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(20);
        
        log.info("Handler: Getting books by uploader: {}", uploadedBy);
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookService.getBooksByUploadedBy(uploadedBy, page, size), Book.class)
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> updateBook(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: Updating book: {}", id);
        
        return validationHandler.validateBody(request, BookUpdateDto.class)
                .flatMap(updateDto -> bookService.updateBook(id, updateDto))
                .flatMap(book -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(book))
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> deleteBook(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: Deleting book: {}", id);
        
        return bookService.deleteBook(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> getTotalBookCount(ServerRequest request) {
        log.info("Handler: Getting total book count");
        
        return bookService.getTotalBookCount()
                .flatMap(count -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(count))
                .onErrorResume(errorRequestHandler::handleError);
    }

    public Mono<ServerResponse> bookExists(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: Checking if book exists: {}", id);
        
        return bookService.bookExists(id)
                .flatMap(exists -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(exists))
                .onErrorResume(errorRequestHandler::handleError);
    }

}