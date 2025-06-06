package kz.readhub.book_management_service.repository;

import kz.readhub.book_management_service.model.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BookRepository extends ReactiveMongoRepository<Book, String> {

    Flux<Book> findByStatus(Book.BookStatus status, Pageable pageable);

    Flux<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Flux<Book> findByAuthorsNameContainingIgnoreCase(String authorName, Pageable pageable);

    Flux<Book> findByCategoriesIn(String[] categories, Pageable pageable);

    Flux<Book> findByLanguage(String language, Pageable pageable);

    Flux<Book> findByUploadedBy(String uploadedBy, Pageable pageable);

    @Query("{ 'status': ?0, 'tags': { $in: ?1 } }")
    Flux<Book> findByStatusAndTagsIn(Book.BookStatus status, String[] tags, Pageable pageable);

    @Query("{ 'status': ?0, 'average_rating': { $gte: ?1 } }")
    Flux<Book> findByStatusAndAverageRatingGreaterThanEqual(Book.BookStatus status, float rating, Pageable pageable);

    @Query("{ 'status': ?0, 'publication_date': { $gte: ?1, $lte: ?2 } }")
    Flux<Book> findByStatusAndPublicationDateBetween(Book.BookStatus status, String startDate, String endDate, Pageable pageable);

    Mono<Boolean> existsByIsbn(String isbn);

    Mono<Long> countByStatus(Book.BookStatus status);

    Mono<Long> countByLanguage(String language);

    Mono<Long> countByCategoriesIn(String[] categories);
}