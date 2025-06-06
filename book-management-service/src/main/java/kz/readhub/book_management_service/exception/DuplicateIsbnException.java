package kz.readhub.book_management_service.exception;

public class DuplicateIsbnException extends BookManagementException {
    
    public DuplicateIsbnException(String isbn) {
        super("Book with ISBN already exists: " + isbn);
    }
}