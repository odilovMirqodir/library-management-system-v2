package uz.example.library.dto.response;

import java.time.LocalDateTime;

public class OverdueLoanResponse {

    private Long loanId;
    private String bookTitle;
    private String isbn;
    private String readerName;
    private String readerPhone;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private long overdueDays;

    public OverdueLoanResponse(
            Long loanId,
            String bookTitle,
            String isbn,
            String readerName,
            String readerPhone,
            LocalDateTime borrowedAt,
            LocalDateTime dueDate,
            long overdueDays
    ) {
        this.loanId = loanId;
        this.bookTitle = bookTitle;
        this.isbn = isbn;
        this.readerName = readerName;
        this.readerPhone = readerPhone;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.overdueDays = overdueDays;
    }

    public Long getLoanId() {
        return loanId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getIsbn() {
        return isbn;
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

    public long getOverdueDays() {
        return overdueDays;
    }
}