package kz.readhub.book_management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class BookManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookManagementServiceApplication.class, args);
    }
}