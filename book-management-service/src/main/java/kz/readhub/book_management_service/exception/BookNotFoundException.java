package kz.readhub.book_management_service.exception;

public class BookNotFoundException extends BookManagementException {
    
    public BookNotFoundException(String bookId) {
        super("Book not found with id: " + bookId);
    }
}