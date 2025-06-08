package kz.readhub.book_management_service.controller;

import kz.readhub.book_management_service.handler.BookHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouteConfiguration {

    @Bean
    public RouterFunction<ServerResponse> bookRoutes(BookHandler bookHandler) {
        return RouterFunctions.route()
                .path("/api/v1/books", builder -> {
                    builder
                            .add(searchRoutes(bookHandler))
                            .add(metaRoutes(bookHandler))
                            .add(coreRoutes(bookHandler));
                })
                .build();
    }

    private RouterFunction<ServerResponse> coreRoutes(BookHandler handler) {
        return RouterFunctions.route()
                .POST("", contentType(MediaType.APPLICATION_JSON), handler::createBook)
                .GET("", handler::getAllBooks)
                .GET("/{id}", handler::getBookById)
                .PUT("/{id}", contentType(MediaType.APPLICATION_JSON), handler::updateBook)
                .DELETE("/{id}", handler::deleteBook)
                .build();
    }

    private RouterFunction<ServerResponse> searchRoutes(BookHandler handler) {
        return RouterFunctions.route()
                .GET("/search", handler::searchBooks)
                .GET("/by-category", handler::getBooksByCategory)
                .GET("/by-language", handler::getBooksByLanguage)
                .GET("/by-uploader", handler::getBooksByUploadedBy)
                .build();
    }

    private RouterFunction<ServerResponse> metaRoutes(BookHandler handler) {
        return RouterFunctions.route()
                .POST("/batch", contentType(MediaType.APPLICATION_JSON), handler::createBooksInBatch)
                .GET("/count", handler::getTotalBookCount)
                .GET("/{id}/exists", handler::bookExists)
                .build();
    }
}