package uz.example.library.service;

import org.springframework.stereotype.Service;
import uz.example.library.dto.response.LibraryReportResponse;
import uz.example.library.entity.Book;
import uz.example.library.enums.BookStatus;
import uz.example.library.enums.LoanStatus;
import uz.example.library.enums.ReaderStatus;
import uz.example.library.exception.BusinessRuleException;
import uz.example.library.repository.BookRepository;
import uz.example.library.repository.LoanRepository;
import uz.example.library.repository.ReaderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;

    public ReportService(
            BookRepository bookRepository,
            ReaderRepository readerRepository,
            LoanRepository loanRepository
    ) {
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.loanRepository = loanRepository;
    }

    public LibraryReportResponse getSummary() {

        List<Book> allBooks =
                bookRepository.findAll();

        List<Book> activeBooks =
                new ArrayList<>();

        for (Book book : allBooks) {

            if (book.getStatus() == BookStatus.ACTIVE) {
                activeBooks.add(book);
            }
        }

        long bookTitles =
                activeBooks.size();

        long totalCopies = 0;
        long availableCopies = 0;

        for (Book book : activeBooks) {

            totalCopies +=
                    book.getTotalCopies();

            availableCopies +=
                    book.getAvailableCopies();
        }

        long borrowedBooks =
                totalCopies - availableCopies;

        if (
                borrowedBooks < 0
                        || borrowedBooks > totalCopies
        ) {
            throw new BusinessRuleException(
                    "Kitob nusxalari holati buzilgan"
            );
        }

        long activeReaders =
                readerRepository
                        .findByStatus(
                                ReaderStatus.ACTIVE
                        )
                        .size();

        long overdueLoans =
                loanRepository
                        .findByStatusAndReturnedAtIsNullAndDueDateBeforeOrderByDueDateAsc(
                                LoanStatus.BORROWED,
                                LocalDateTime.now()
                        )
                        .size();

        return new LibraryReportResponse(
                bookTitles,
                totalCopies,
                availableCopies,
                borrowedBooks,
                activeReaders,
                overdueLoans
        );
    }
}