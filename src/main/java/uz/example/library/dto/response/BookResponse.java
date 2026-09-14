package uz.example.library.dto.response;

import uz.example.library.enums.BookStatus;

import java.time.LocalDateTime;

public class BookResponse {

    private Long id;
    private String isbn;
    private String title;

    private Long authorId;
    private String authorName;

    private Long categoryId;
    private String categoryName;

    private Integer publishedYear;
    private Integer totalCopies;
    private Integer availableCopies;

    private BookStatus status;
    private LocalDateTime createdAt;

    public BookResponse(
            Long id,
            String isbn,
            String title,
            Long authorId,
            String authorName,
            Long categoryId,
            String categoryName,
            Integer publishedYear,
            Integer totalCopies,
            Integer availableCopies,
            BookStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.publishedYear = publishedYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public BookStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}