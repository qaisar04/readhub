package kz.readhub.book_management_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kz.readhub.book_management_service.dto.BookCreateDto;
import kz.readhub.book_management_service.dto.BookSearchDto;
import kz.readhub.book_management_service.dto.BookUpdateDto;
import kz.readhub.book_management_service.handler.BookErrorRequestHandler;
import kz.readhub.book_management_service.handler.BookHandler;
import kz.readhub.book_management_service.handler.ErrorRequestHandler;
import kz.readhub.book_management_service.model.Book;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouteConfiguration {

    @Bean
    public ErrorRequestHandler errorRequestHandler() {
        return new BookErrorRequestHandler();
    }

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/search",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "searchBooks", summary = "Search books",
                            tags = {"Books - Search"},
                            requestBody = @RequestBody(
                                    content = @Content(
                                            schema = @Schema(
                                                    implementation = BookSearchDto.class
                                            ))
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(
                                    schema = @Schema(
                                            implementation = Book.class
                                    ))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/search/by-category",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "getBooksByCategory", summary = "Get books by category",
                            tags = {"Books - Search"},
                            requestBody = @RequestBody(
                                    content = @Content(
                                            schema = @Schema(
                                                    implementation = BookSearchDto.class
                                            ))
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(
                                    schema = @Schema(implementation = Book.class
                                    ))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/search/by-language",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "getBooksByLanguage", summary = "Get books by language",
                            tags = {"Books - Search"},
                            requestBody = @RequestBody(
                                    content = @Content(
                                            schema = @Schema(
                                                    implementation = BookSearchDto.class
                                            ))
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(
                                    schema = @Schema(
                                            implementation = Book.class
                                    ))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/search/by-uploader",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "getBooksByUploader", summary = "Get books by uploader",
                            tags = {"Books - Search"},
                            requestBody = @RequestBody(
                                    content = @Content(
                                            schema = @Schema(
                                                    implementation = BookSearchDto.class
                                            ))
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(
                                    schema = @Schema(
                                            implementation = Book.class
                                    ))
                            )
                    )
            )
    })
    public RouterFunction<ServerResponse> searchRoutes(BookHandler handler) {
        return RouterFunctions.route()
                .POST("/search", contentType(MediaType.APPLICATION_JSON), handler::searchBooks)
                .POST("/by-category", contentType(MediaType.APPLICATION_JSON), handler::getBooksByCategory)
                .POST("/by-language", contentType(MediaType.APPLICATION_JSON), handler::getBooksByLanguage)
                .POST("/by-uploader", contentType(MediaType.APPLICATION_JSON), handler::getBooksByUploadedBy)
                .build();
    }

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/books/count",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getTotalBookCount", summary = "Get total book count",
                            tags = {"Books - Meta"},
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(schema = @Schema(type = "integer"))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/books/{id}/exists",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "bookExists", summary = "Check if book exists",
                            tags = {"Books - Meta"},
                            parameters = @Parameter(
                                    name = "id", in = ParameterIn.PATH, required = true
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(schema = @Schema(type = "boolean"))
                            )
                    )
            )
    })
    public RouterFunction<ServerResponse> metaRoutes(BookHandler handler) {
        return RouterFunctions.route()
                .GET("/books/count", handler::getTotalBookCount)
                .GET("/books/{id}/exists", handler::bookExists)
                .build();
    }

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/books",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "createBook", summary = "Create a new book",
                            tags = {"Books - Core"},
                            requestBody = @RequestBody(
                                    content = @Content(
                                            schema = @Schema(
                                                    implementation = BookCreateDto.class
                                            )
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200", content = @Content(
                                            schema = @Schema(
                                                    implementation = Book.class
                                            )
                                    )),
                                    @ApiResponse(
                                            responseCode = "400", content = @Content(schema = @Schema(implementation = BookErrorRequestHandler.class))
                                    )
                            })),
            @RouterOperation(path = "/books", method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getAllBooks", summary = "Get all books with pagination",
                            tags = {"Books - Core"},
                            parameters = {
                                    @Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
                                    @Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20")),
                                    @Parameter(name = "sortBy", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "createdAt")),
                                    @Parameter(name = "sortDirection", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "desc"))
                            },
                            responses = @ApiResponse(
                                    responseCode = "200", content = @Content(
                                    schema = @Schema(
                                            implementation = Book.class
                                    ))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/books/{id}",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getBookById", summary = "Get book by ID",
                            tags = {"Books - Core"},
                            parameters = @Parameter(
                                    name = "id", in = ParameterIn.PATH, required = true
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200", content = @Content(
                                            schema = @Schema(
                                                    implementation = Book.class
                                            ))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404", content = @Content(
                                            schema = @Schema(
                                                    implementation = BookErrorRequestHandler.class
                                            ))
                                    )
                            })),
            @RouterOperation(
                    path = "/books/{id}",
                    method = RequestMethod.PUT,
                    operation = @Operation(
                            operationId = "updateBook", summary = "Update book by ID",
                            tags = {"Books - Core"},

                            parameters = @Parameter(
                                    name = "id", in = ParameterIn.PATH, required = true
                            ),
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(
                                            implementation = BookUpdateDto.class
                                    )
                            )),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200", content = @Content(
                                            schema = @Schema(
                                                    implementation = Book.class
                                            ))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404", content = @Content(
                                            schema = @Schema(
                                                    implementation = BookErrorRequestHandler.class
                                            ))
                                    )
                            })),
            @RouterOperation(
                    path = "/books/{id}",
                    method = RequestMethod.DELETE,
                    operation = @Operation(
                            operationId = "deleteBook", summary = "Delete book by ID",
                            tags = {"Books - Core"},
                            parameters = @Parameter(
                                    name = "id", in = ParameterIn.PATH, required = true
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "204"
                                    ),
                                    @ApiResponse(
                                            responseCode = "404", content = @Content(
                                            schema = @Schema(
                                                    implementation = BookErrorRequestHandler.class
                                            ))
                                    )
                            }))
    })
    public RouterFunction<ServerResponse> coreRoutes(BookHandler handler) {
        return RouterFunctions.route()
                .POST("", contentType(MediaType.APPLICATION_JSON), handler::createBook)
                .GET("", handler::getAllBooks)
                .GET("/{id}", handler::getBookById)
                .PUT("/{id}", contentType(MediaType.APPLICATION_JSON), handler::updateBook)
                .DELETE("/{id}", handler::deleteBook)
                .build();
    }
}