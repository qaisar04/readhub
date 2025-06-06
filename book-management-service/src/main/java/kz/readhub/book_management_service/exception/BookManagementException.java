package kz.readhub.book_management_service.exception;

public class BookManagementException extends RuntimeException {
    
    public BookManagementException(String message) {
        super(message);
    }
    
    public BookManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}