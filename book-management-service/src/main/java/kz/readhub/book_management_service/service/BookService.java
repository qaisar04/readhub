package kz.readhub.book_management_service.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.readhub.book_management_service.dto.BookCreateDto;
import kz.readhub.book_management_service.dto.BookUpdateDto;
import kz.readhub.book_management_service.exception.BookNotFoundException;
import kz.readhub.book_management_service.exception.DuplicateIsbnException;
import kz.readhub.book_management_service.model.Book;
import kz.readhub.book_management_service.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

 import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Service layer for book management operations.
 * Implements domain logic, validation, and orchestrates repository and messaging operations.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;
    private final KafkaPublisherService kafkaPublisherService;

    /**
     * Creates a new book with validation and event publishing.
     * 
     * @param createDto the book creation data
     * @return Mono containing the created book
     */
    @Transactional
    public Mono<Book> createBook(@Valid @NotNull BookCreateDto createDto) {
        log.info("Creating new book with title: {}", createDto.getTitle());
        
        return validateIsbnUniqueness(createDto.getIsbn())
                .then(Mono.fromCallable(() -> mapToNewBook(createDto)))
                .flatMap(bookRepository::save)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(savedBook -> {
                    log.info("Successfully created book with id: {}", savedBook.getId());
                    kafkaPublisherService.publishBookCreatedEvent(savedBook).subscribe();
                })
                .doOnError(error -> log.error("Failed to create book: {}", createDto.getTitle(), error));
    }

    /**
     * Retrieves a book by its ID.
     * 
     * @param id the book ID
     * @return Mono containing the book
     * @throws BookNotFoundException if book not found
     */
    public Mono<Book> getBookById(@NotBlank String id) {
        log.info("Getting book by id: {}", id);
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new BookNotFoundException(id)));
    }

    public Flux<Book> getAllBooks(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all books - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return bookRepository.findByStatus(Book.BookStatus.ACTIVE, pageable);
    }

    public Flux<Book> searchBooks(String query, int page, int size) {
        log.info("Searching books with query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        
        if (query == null || query.trim().isEmpty()) {
            return bookRepository.findByStatus(Book.BookStatus.ACTIVE, pageable);
        }
        
        return bookRepository.findByTitleContainingIgnoreCase(query, pageable)
                .mergeWith(bookRepository.findByAuthorsNameContainingIgnoreCase(query, pageable))
                .distinct()
                .take(size);
    }

    public Flux<Book> getBooksByCategory(String[] categories, int page, int size) {
        log.info("Getting books by categories: {}", String.join(",", categories));
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findByCategoriesIn(categories, pageable);
    }

    public Flux<Book> getBooksByLanguage(String language, int page, int size) {
        log.info("Getting books by language: {}", language);
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findByLanguage(language, pageable);
    }

    public Flux<Book> getBooksByUploadedBy(String uploadedBy, int page, int size) {
        log.info("Getting books uploaded by: {}", uploadedBy);
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findByUploadedBy(uploadedBy, pageable);
    }

    public Mono<Book> updateBook(String id, BookUpdateDto updateDto) {
        log.info("Updating book with id: {}", id);
        
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new BookNotFoundException(id)))
                .flatMap(existingBook -> {
                    if (updateDto.getIsbn() != null && !updateDto.getIsbn().equals(existingBook.getIsbn())) {
                        return validateIsbnUniqueness(updateDto.getIsbn())
                                .then(Mono.just(existingBook));
                    }
                    return Mono.just(existingBook);
                })
                .map(existingBook -> applyUpdates(existingBook, updateDto))
                .flatMap(bookRepository::save)
                .doOnSuccess(updatedBook -> {
                    log.info("Successfully updated book with id: {}", updatedBook.getId());
                    kafkaPublisherService.publishBookUpdatedEvent(updatedBook).subscribe();
                })
                .doOnError(error -> log.error("Failed to update book: {}", id, error));
    }

    public Mono<Void> deleteBook(String id) {
        log.info("Deleting book with id: {}", id);
        
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new BookNotFoundException(id)))
                .map(this::markAsDeleted)
                .flatMap(bookRepository::save)
                .doOnSuccess(deletedBook -> {
                    log.info("Successfully deleted book with id: {}", deletedBook.getId());
                    kafkaPublisherService.publishBookDeletedEvent(deletedBook).subscribe();
                })
                .doOnError(error -> log.error("Failed to delete book: {}", id, error))
                .then();
    }

    public Mono<Long> getTotalBookCount() {
        return bookRepository.countByStatus(Book.BookStatus.ACTIVE);
    }

    public Mono<Boolean> bookExists(String id) {
        return bookRepository.existsById(id);
    }

    public Flux<Book> createBooks(List<BookCreateDto> createDtos) {
        log.info("Creating {} books in batch", createDtos.size());
        
        return Flux.fromIterable(createDtos)
                .flatMap(createDto -> validateIsbnUniqueness(createDto.getIsbn())
                        .then(Mono.fromCallable(() -> mapToNewBook(createDto))))
                .collectList()
                .flatMapMany(books -> bookRepository.saveAll(books)
                        .collectList()
                        .doOnSuccess(savedBooks -> {
                            log.info("Successfully created {} books in batch", savedBooks.size());
                            // TODO: Publish batch creation event
                        })
                        .flatMapMany(Flux::fromIterable))
                .doOnError(error -> log.error("Failed to create books in batch", error));
    }

    private Mono<Void> validateIsbnUniqueness(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Mono.empty();
        }
        
        return bookRepository.existsByIsbn(isbn)
                .flatMap(exists -> exists 
                        ? Mono.error(new DuplicateIsbnException(isbn))
                        : Mono.empty());
    }

    private Book mapToNewBook(BookCreateDto createDto) {
        Book book = modelMapper.map(createDto, Book.class);
        return book.toBuilder()
                .status(Book.BookStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Book applyUpdates(Book existingBook, BookUpdateDto updateDto) {
        Book.BookBuilder builder = existingBook.toBuilder()
                .updatedAt(LocalDateTime.now());

        return builder
                .title(getValueOrDefault(updateDto.getTitle(), existingBook.getTitle()))
                .description(getValueOrDefault(updateDto.getDescription(), existingBook.getDescription()))
                .authors(getValueOrDefault(updateDto.getAuthors(), existingBook.getAuthors()))
                .tags(updateDto.getTags() != null ? Set.copyOf(updateDto.getTags()) : existingBook.getTags())
                .categories(updateDto.getCategories() != null ? Set.copyOf(updateDto.getCategories()) : existingBook.getCategories())
                .language(getValueOrDefault(updateDto.getLanguage(), existingBook.getLanguage()))
                .publicationDate(getValueOrDefault(updateDto.getPublicationDate(), existingBook.getPublicationDate()))
                .coverUrl(getValueOrDefault(updateDto.getCoverUrl(), existingBook.getCoverUrl()))
                .filePath(getValueOrDefault(updateDto.getFilePath(), existingBook.getFilePath()))
                .fileSize(getValueOrDefault(updateDto.getFileSize(), existingBook.getFileSize()))
                .isbn(getValueOrDefault(updateDto.getIsbn(), existingBook.getIsbn()))
                .publisher(getValueOrDefault(updateDto.getPublisher(), existingBook.getPublisher()))
                .pageCount(getValueOrDefault(updateDto.getPageCount(), existingBook.getPageCount()))
                .averageRating(updateDto.getAverageRating() != null ? updateDto.getAverageRating().doubleValue() : existingBook.getAverageRating())
                .reviewCount(getValueOrDefault(updateDto.getReviewCount(), existingBook.getReviewCount()))
                .downloadCount(getValueOrDefault(updateDto.getDownloadCount(), existingBook.getDownloadCount()))
                .status(getValueOrDefault(updateDto.getStatus(), existingBook.getStatus()))
                .build();
    }

    private Book markAsDeleted(Book book) {
        return book.toBuilder()
                .status(Book.BookStatus.DELETED)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private <T> T getValueOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}