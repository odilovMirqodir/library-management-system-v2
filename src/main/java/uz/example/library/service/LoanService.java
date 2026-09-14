package uz.example.library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.example.library.dto.request.BorrowRequest;
import uz.example.library.dto.response.LoanResponse;
import uz.example.library.dto.response.OverdueLoanResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.entity.Book;
import uz.example.library.entity.Loan;
import uz.example.library.entity.Reader;
import uz.example.library.enums.BookStatus;
import uz.example.library.enums.LoanStatus;
import uz.example.library.enums.ReaderStatus;
import uz.example.library.exception.BusinessRuleException;
import uz.example.library.exception.EntityNotFoundException;
import uz.example.library.exception.ValidationException;
import uz.example.library.mapper.LoanMapper;
import uz.example.library.repository.BookRepository;
import uz.example.library.repository.LoanRepository;
import uz.example.library.repository.ReaderRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final LoanMapper loanMapper;

    public LoanService(
            LoanRepository loanRepository,
            BookRepository bookRepository,
            ReaderRepository readerRepository,
            LoanMapper loanMapper
    ) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.loanMapper = loanMapper;
    }

    @Transactional
    public LoanResponse borrowBook(
            BorrowRequest request
    ) {

        Reader reader =
                readerRepository.findById(
                                request.getReaderId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitobxon topilmadi"
                                )
                        );

        Book book =
                bookRepository.findById(
                                request.getBookId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitob topilmadi"
                                )
                        );

        if (reader.getStatus() != ReaderStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Faqat ACTIVE kitobxon kitob olishi mumkin"
            );
        }

        if (book.getStatus() != BookStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Faqat ACTIVE kitobni olish mumkin"
            );
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException(
                    "Kitobning mavjud nusxasi qolmagan"
            );
        }

        long activeLoanCount =
                loanRepository.countByReaderIdAndStatus(
                        reader.getId(),
                        LoanStatus.BORROWED
                );

        if (activeLoanCount >= 5) {
            throw new BusinessRuleException(
                    "Kitobxon bir vaqtning o'zida 5 tadan ortiq kitob ololmaydi"
            );
        }

        boolean alreadyBorrowed =
                loanRepository
                        .existsByReaderIdAndBookIsbnAndStatus(
                                reader.getId(),
                                book.getIsbn(),
                                LoanStatus.BORROWED
                        );

        if (alreadyBorrowed) {
            throw new BusinessRuleException(
                    "Kitobxon ayni ISBN dagi kitobni qaytarmasdan yana ololmaydi"
            );
        }

        LocalDateTime borrowedAt =
                LocalDateTime.now();

        Loan loan = new Loan();

        loan.setReader(reader);
        loan.setBook(book);
        loan.setBorrowedAt(borrowedAt);
        loan.setDueDate(
                borrowedAt.plusDays(14)
        );
        loan.setReturnedAt(null);
        loan.setStatus(
                LoanStatus.BORROWED
        );

        book.setAvailableCopies(
                book.getAvailableCopies() - 1
        );

        bookRepository.save(book);

        Loan savedLoan =
                loanRepository.save(loan);

        return loanMapper.toResponse(
                savedLoan
        );
    }

    @Transactional
    public LoanResponse returnBook(
            Long loanId
    ) {

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Qarz topilmadi"
                                )
                        );

        if (loan.getStatus() != LoanStatus.BORROWED) {
            throw new BusinessRuleException(
                    "Bu kitob allaqachon qaytarilgan"
            );
        }

        Book book =
                loan.getBook();

        if (
                book.getAvailableCopies()
                        >= book.getTotalCopies()
        ) {
            throw new BusinessRuleException(
                    "Kitob nusxalari holati noto'g'ri"
            );
        }

        loan.setReturnedAt(
                LocalDateTime.now()
        );

        loan.setStatus(
                LoanStatus.RETURNED
        );

        book.setAvailableCopies(
                book.getAvailableCopies() + 1
        );

        bookRepository.save(book);

        Loan updatedLoan =
                loanRepository.save(loan);

        return loanMapper.toResponse(
                updatedLoan
        );
    }

    public PageResponse<LoanResponse> findAll(
            LoanStatus status,
            Long readerId,
            Long bookId,
            String sort,
            int page,
            int size
    ) {

        validatePagination(
                page,
                size
        );

        List<Loan> loans =
                new ArrayList<>(
                        loanRepository.findWithFilters(
                                status,
                                readerId,
                                bookId
                        )
                );

        applyLoanSort(
                loans,
                sort
        );

        long totalElements =
                loans.size();

        long start =
                (long) page * size;

        List<Loan> pageLoans;

        if (start >= loans.size()) {

            pageLoans =
                    new ArrayList<>();

        } else {

            int fromIndex =
                    (int) start;

            int toIndex =
                    Math.min(
                            fromIndex + size,
                            loans.size()
                    );

            pageLoans =
                    loans.subList(
                            fromIndex,
                            toIndex
                    );
        }

        List<LoanResponse> responses =
                new ArrayList<>();

        for (Loan loan : pageLoans) {

            responses.add(
                    loanMapper.toResponse(loan)
            );
        }

        int totalPages =
                (int) Math.ceil(
                        (double) totalElements / size
                );

        return new PageResponse<>(
                responses,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    public PageResponse<OverdueLoanResponse> findOverdue(
            String sort,
            int page,
            int size
    ) {

        validatePagination(
                page,
                size
        );

        List<Loan> loans =
                new ArrayList<>(
                        loanRepository
                                .findByStatusAndReturnedAtIsNullAndDueDateBeforeOrderByDueDateAsc(
                                        LoanStatus.BORROWED,
                                        LocalDateTime.now()
                                )
                );

        applyOverdueSort(
                loans,
                sort
        );

        long totalElements =
                loans.size();

        long start =
                (long) page * size;

        List<Loan> pageLoans;

        if (start >= loans.size()) {

            pageLoans =
                    new ArrayList<>();

        } else {

            int fromIndex =
                    (int) start;

            int toIndex =
                    Math.min(
                            fromIndex + size,
                            loans.size()
                    );

            pageLoans =
                    loans.subList(
                            fromIndex,
                            toIndex
                    );
        }

        List<OverdueLoanResponse> responses =
                new ArrayList<>();

        LocalDateTime now =
                LocalDateTime.now();

        for (Loan loan : pageLoans) {

            long overdueDays =
                    ChronoUnit.DAYS.between(
                            loan.getDueDate(),
                            now
                    );

            responses.add(
                    new OverdueLoanResponse(
                            loan.getId(),
                            loan.getBook().getTitle(),
                            loan.getBook().getIsbn(),
                            loan.getReader().getFullName(),
                            loan.getReader().getPhone(),
                            loan.getBorrowedAt(),
                            loan.getDueDate(),
                            overdueDays
                    )
            );
        }

        int totalPages =
                (int) Math.ceil(
                        (double) totalElements / size
                );

        return new PageResponse<>(
                responses,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new ValidationException(
                    "Page 0 dan kichik bo'lishi mumkin emas"
            );
        }

        if (size < 1 || size > 100) {
            throw new ValidationException(
                    "Size 1 dan 100 gacha bo'lishi kerak"
            );
        }
    }

    private void applyLoanSort(
            List<Loan> loans,
            String sort
    ) {

        if (
                sort == null
                        || sort.isBlank()
                        || sort.equalsIgnoreCase("borrowedAt")
                        || sort.equalsIgnoreCase("borrowedAtDesc")
        ) {

            loans.sort(
                    Comparator.comparing(
                                    Loan::getBorrowedAt
                            )
                            .reversed()
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("borrowedAtAsc")
        ) {

            loans.sort(
                    Comparator.comparing(
                            Loan::getBorrowedAt
                    )
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("dueDate")
                        || sort.equalsIgnoreCase("dueDateAsc")
        ) {

            loans.sort(
                    Comparator.comparing(
                            Loan::getDueDate
                    )
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("dueDateDesc")
        ) {

            loans.sort(
                    Comparator.comparing(
                                    Loan::getDueDate
                            )
                            .reversed()
            );

            return;
        }

        throw new ValidationException(
                "Sort qiymati noto'g'ri. borrowedAt, borrowedAtAsc, dueDate yoki dueDateDesc ishlating"
        );
    }

    private void applyOverdueSort(
            List<Loan> loans,
            String sort
    ) {

        if (
                sort == null
                        || sort.isBlank()
                        || sort.equalsIgnoreCase("dueDate")
                        || sort.equalsIgnoreCase("dueDateAsc")
                        || sort.equalsIgnoreCase("overdue")
        ) {

            loans.sort(
                    Comparator.comparing(
                            Loan::getDueDate
                    )
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("dueDateDesc")
        ) {

            loans.sort(
                    Comparator.comparing(
                                    Loan::getDueDate
                            )
                            .reversed()
            );

            return;
        }

        throw new ValidationException(
                "Sort qiymati noto'g'ri. dueDate yoki dueDateDesc ishlating"
        );
    }
}