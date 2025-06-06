package kz.readhub.book_management_service.service;

import kz.readhub.book_management_service.dto.BookCreateDto;
import kz.readhub.book_management_service.dto.BookUpdateDto;
import kz.readhub.book_management_service.exception.BookNotFoundException;
import kz.readhub.book_management_service.exception.DuplicateIsbnException;
import kz.readhub.book_management_service.model.Author;
import kz.readhub.book_management_service.model.Book;
import kz.readhub.book_management_service.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for BookService.
 * Tests business logic, validation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private KafkaPublisherService kafkaPublisherService;

    @Mock
    private BatchBookEventService batchBookEventService;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private BookCreateDto testCreateDto;
    private BookUpdateDto testUpdateDto;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id("test-id")
                .title("Test Book")
                .description("Test Description")
                .authors(List.of(Author.builder()
                        .name("Test Author")
                        .biography("Test Bio")
                        .build()))
                .language("en")
                .uploadedBy("test-user")
                .isbn("978-0123456789")
                .status(Book.BookStatus.ACTIVE)
                .averageRating(4.5)
                .reviewCount(10)
                .downloadCount(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCreateDto = BookCreateDto.builder()
                .title("Test Book")
                .description("Test Description")
                .authors(List.of(Author.builder()
                        .name("Test Author")
                        .build()))
                .language("en")
                .userId("test-user")
                .filePath("/test/path")
                .categories(List.of("Test Category"))
                .isbn("978-0123456789")
                .build();

        testUpdateDto = BookUpdateDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .userId("test-user")
                .build();
    }

    @Nested
    @DisplayName("Create Book Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully")
        void shouldCreateBookSuccessfully() {
            // Given
            when(bookRepository.existsByIsbn(anyString())).thenReturn(Mono.just(false));
            when(modelMapper.map(any(BookCreateDto.class), eq(Book.class))).thenReturn(testBook);
            when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(testBook));
            when(kafkaPublisherService.publishBookCreatedEvent(any(Book.class))).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(bookService.createBook(testCreateDto))
                    .expectNext(testBook)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should throw DuplicateIsbnException when ISBN exists")
        void shouldThrowDuplicateIsbnExceptionWhenIsbnExists() {
            // Given
            when(bookRepository.existsByIsbn(anyString())).thenReturn(Mono.just(true));

            // When & Then
            StepVerifier.create(bookService.createBook(testCreateDto))
                    .expectError(DuplicateIsbnException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Get Book Tests")
    class GetBookTests {

        @Test
        @DisplayName("Should get book by ID successfully")
        void shouldGetBookByIdSuccessfully() {
            // Given
            when(bookRepository.findById(anyString())).thenReturn(Mono.just(testBook));

            // When & Then
            StepVerifier.create(bookService.getBookById("test-id"))
                    .expectNext(testBook)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when book not found")
        void shouldThrowBookNotFoundExceptionWhenBookNotFound() {
            // Given
            when(bookRepository.findById(anyString())).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(bookService.getBookById("non-existent-id"))
                    .expectError(BookNotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Search and Filter Tests")
    class SearchAndFilterTests {

        @Test
        @DisplayName("Should get all books with pagination")
        void shouldGetAllBooksWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"));
            when(bookRepository.findByStatus(eq(Book.BookStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(Flux.just(testBook));

            // When & Then
            StepVerifier.create(bookService.getAllBooks(0, 10, "title", "asc"))
                    .expectNext(testBook)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should search books by query")
        void shouldSearchBooksByQuery() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            when(bookRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                    .thenReturn(Flux.just(testBook));
            when(bookRepository.findByAuthorsNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                    .thenReturn(Flux.empty());

            // When & Then
            StepVerifier.create(bookService.searchBooks("test", 0, 10))
                    .expectNext(testBook)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Update Book Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBookSuccessfully() {
            // Given
            Book updatedBook = testBook.toBuilder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .build();

            when(bookRepository.findById(anyString())).thenReturn(Mono.just(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(updatedBook));
            when(kafkaPublisherService.publishBookUpdatedEvent(any(Book.class))).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(bookService.updateBook("test-id", testUpdateDto))
                    .expectNext(updatedBook)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Delete Book Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book successfully")
        void shouldDeleteBookSuccessfully() {
            // Given
            Book deletedBook = testBook.toBuilder()
                    .status(Book.BookStatus.ARCHIVED)
                    .build();

            when(bookRepository.findById(anyString())).thenReturn(Mono.just(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(deletedBook));
            when(kafkaPublisherService.publishBookDeletedEvent(any(Book.class))).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(bookService.deleteBook("test-id"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Utility Tests")
    class UtilityTests {

        @Test
        @DisplayName("Should get total book count")
        void shouldGetTotalBookCount() {
            // Given
            when(bookRepository.countByStatus(Book.BookStatus.ACTIVE)).thenReturn(Mono.just(5L));

            // When & Then
            StepVerifier.create(bookService.getTotalBookCount())
                    .expectNext(5L)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should check if book exists")
        void shouldCheckIfBookExists() {
            // Given
            when(bookRepository.existsById(anyString())).thenReturn(Mono.just(true));

            // When & Then
            StepVerifier.create(bookService.bookExists("test-id"))
                    .expectNext(true)
                    .verifyComplete();
        }
    }
}