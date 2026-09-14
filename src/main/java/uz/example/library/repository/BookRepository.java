package uz.example.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.example.library.entity.Book;
import uz.example.library.enums.BookStatus;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(
            String isbn,
            Long id
    );

    boolean existsByAuthorId(
            Long authorId
    );

    boolean existsByCategoryId(
            Long categoryId
    );

    @Query("""
            SELECT b
            FROM Book b
            WHERE b.status = :status
              AND (
                    :title IS NULL
                    OR LOWER(b.title) LIKE CONCAT(
                        '%',
                        LOWER(CAST(:title AS string)),
                        '%'
                    )
              )
              AND (
                    :isbn IS NULL
                    OR b.isbn = :isbn
              )
              AND (
                    :authorId IS NULL
                    OR b.author.id = :authorId
              )
              AND (
                    :categoryId IS NULL
                    OR b.category.id = :categoryId
              )
              AND (
                    :available IS NULL
                    OR (
                        :available = true
                        AND b.availableCopies > 0
                    )
                    OR (
                        :available = false
                        AND b.availableCopies = 0
                    )
              )
            """)
    List<Book> findWithFilters(
            @Param("title") String title,
            @Param("isbn") String isbn,
            @Param("authorId") Long authorId,
            @Param("categoryId") Long categoryId,
            @Param("available") Boolean available,
            @Param("status") BookStatus status
    );
}