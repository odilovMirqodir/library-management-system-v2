package uz.example.library.dto.response;

import uz.example.library.enums.LoanStatus;

import java.time.LocalDateTime;

public class LoanResponse {

    private Long id;

    private Long bookId;
    private String bookTitle;
    private String isbn;

    private Long readerId;
    private String readerName;
    private String readerPhone;

    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;

    private LoanStatus status;

    public LoanResponse(
            Long id,
            Long bookId,
            String bookTitle,
            String isbn,
            Long readerId,
            String readerName,
            String readerPhone,
            LocalDateTime borrowedAt,
            LocalDateTime dueDate,
            LocalDateTime returnedAt,
            LoanStatus status
    ) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.isbn = isbn;
        this.readerId = readerId;
        this.readerName = readerName;
        this.readerPhone = readerPhone;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getIsbn() {
        return isbn;
    }

    public Long getReaderId() {
        return readerId;
    }

    public String getReaderName() {
        return readerName;
    }

    public String getReaderPhone() {
        return readerPhone;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public LoanStatus getStatus() {
        return status;
    }
}