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
        return RouterFunctions
                .route()
                .path("/api/v1/books", builder -> builder
                        .POST("",
                                accept(MediaType.APPLICATION_JSON)
                                        .and(contentType(MediaType.APPLICATION_JSON)),
                                bookHandler::createBook)
                        .GET("/{id}",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::getBookById)
                        .GET("",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::getAllBooks)
                        .PUT("/{id}",
                                accept(MediaType.APPLICATION_JSON)
                                        .and(contentType(MediaType.APPLICATION_JSON)),
                                bookHandler::updateBook)
                        .DELETE("/{id}",
                                bookHandler::deleteBook)
                        .GET("/search",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::searchBooks)
                        .GET("/by-category",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::getBooksByCategory)
                        .GET("/by-language",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::getBooksByLanguage)
                        .GET("/by-uploader",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::getBooksByUploadedBy)
                        .POST("/batch",
                                accept(MediaType.APPLICATION_JSON)
                                        .and(contentType(MediaType.APPLICATION_JSON)),
                                bookHandler::createBooksInBatch)
                        .GET("/count",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::getTotalBookCount)
                        .GET("/{id}/exists",
                                accept(MediaType.APPLICATION_JSON),
                                bookHandler::bookExists)
                )
                .build();
    }
}