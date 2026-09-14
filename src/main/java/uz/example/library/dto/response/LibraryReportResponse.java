package uz.example.library.dto.response;

public class LibraryReportResponse {

    private long bookTitles;
    private long totalCopies;
    private long availableCopies;
    private long borrowedBooks;
    private long activeReaders;
    private long overdueLoans;

    public LibraryReportResponse(
            long bookTitles,
            long totalCopies,
            long availableCopies,
            long borrowedBooks,
            long activeReaders,
            long overdueLoans
    ) {
        this.bookTitles = bookTitles;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.borrowedBooks = borrowedBooks;
        this.activeReaders = activeReaders;
        this.overdueLoans = overdueLoans;
    }

    public long getBookTitles() {
        return bookTitles;
    }

    public long getTotalCopies() {
        return totalCopies;
    }

    public long getAvailableCopies() {
        return availableCopies;
    }

    public long getBorrowedBooks() {
        return borrowedBooks;
    }

    public long getActiveReaders() {
        return activeReaders;
    }

    public long getOverdueLoans() {
        return overdueLoans;
    }
}