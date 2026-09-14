package uz.example.library.mapper;

import org.springframework.stereotype.Component;
import uz.example.library.dto.request.BookCreateRequest;
import uz.example.library.dto.response.BookResponse;
import uz.example.library.entity.Author;
import uz.example.library.entity.Book;
import uz.example.library.entity.Category;
import uz.example.library.dto.request.BookUpdateRequest;

@Component
public class BookMapper {

    public Book toEntity(
            BookCreateRequest request,
            Author author,
            Category category
    ) {
        Book book = new Book();

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(author);
        book.setCategory(category);
        book.setPublishedYear(request.getPublishedYear());
        book.setTotalCopies(request.getTotalCopies());

        return book;
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor().getId(),
                book.getAuthor().getFullName(),
                book.getCategory().getId(),
                book.getCategory().getName(),
                book.getPublishedYear(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getStatus(),
                book.getCreatedAt()
        );
    }

    public void updateEntity(
            Book book,
            BookUpdateRequest request,
            Author author,
            Category category
    ) {
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(author);
        book.setCategory(category);
        book.setPublishedYear(request.getPublishedYear());
        book.setTotalCopies(request.getTotalCopies());
    }
}