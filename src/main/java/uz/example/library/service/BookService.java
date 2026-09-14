package uz.example.library.service;

import org.springframework.stereotype.Service;
import uz.example.library.dto.request.BookCreateRequest;
import uz.example.library.dto.request.BookUpdateRequest;
import uz.example.library.dto.response.BookResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.entity.Author;
import uz.example.library.entity.Book;
import uz.example.library.entity.Category;
import uz.example.library.enums.BookStatus;
import uz.example.library.enums.LoanStatus;
import uz.example.library.exception.BusinessRuleException;
import uz.example.library.exception.DuplicateEntityException;
import uz.example.library.exception.EntityNotFoundException;
import uz.example.library.exception.ValidationException;
import uz.example.library.mapper.BookMapper;
import uz.example.library.repository.AuthorRepository;
import uz.example.library.repository.BookRepository;
import uz.example.library.repository.CategoryRepository;
import uz.example.library.repository.LoanRepository;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final LoanRepository loanRepository;

    public BookService(
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            CategoryRepository categoryRepository,
            BookMapper bookMapper,
            LoanRepository loanRepository
    ) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookMapper = bookMapper;
        this.loanRepository = loanRepository;
    }

    public BookResponse create(
            BookCreateRequest request
    ) {

        String isbn = normalizeIsbn(
                request.getIsbn()
        );

        if (!isbn.matches("\\d{10}|\\d{13}")) {
            throw new ValidationException(
                    "ISBN 10 yoki 13 ta raqamdan iborat bo'lishi kerak"
            );
        }

        if (bookRepository.existsByIsbn(isbn)) {
            throw new DuplicateEntityException(
                    "Bunday ISBN bilan kitob allaqachon mavjud"
            );
        }

        int currentYear =
                Year.now().getValue();

        if (request.getPublishedYear() > currentYear) {
            throw new ValidationException(
                    "Nashr yili kelajakdagi yil bo'lishi mumkin emas"
            );
        }

        Author author =
                authorRepository
                        .findById(request.getAuthorId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Muallif topilmadi"
                                )
                        );

        Category category =
                categoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kategoriya topilmadi"
                                )
                        );

        Book book =
                bookMapper.toEntity(
                        request,
                        author,
                        category
                );

        book.setIsbn(isbn);

        book.setTitle(
                request.getTitle().trim()
        );

        book.setAvailableCopies(
                request.getTotalCopies()
        );

        book.setStatus(
                BookStatus.ACTIVE
        );

        book.setCreatedAt(
                LocalDateTime.now()
        );

        Book savedBook =
                bookRepository.save(book);

        return bookMapper.toResponse(
                savedBook
        );
    }

    public PageResponse<BookResponse> findAll(
            String title,
            String isbn,
            Long authorId,
            Long categoryId,
            Boolean available,
            BookStatus status,
            String sort,
            int page,
            int size
    ) {

        validatePagination(
                page,
                size
        );

        BookStatus finalStatus;

        if (status == null) {
            finalStatus =
                    BookStatus.ACTIVE;
        } else {
            finalStatus =
                    status;
        }

        String normalizedTitle =
                null;

        if (
                title != null
                        && !title.isBlank()
        ) {
            normalizedTitle =
                    title.trim();
        }

        String normalizedIsbn =
                null;

        if (
                isbn != null
                        && !isbn.isBlank()
        ) {
            normalizedIsbn =
                    normalizeIsbn(isbn);
        }

        List<Book> books =
                new ArrayList<>(
                        bookRepository.findWithFilters(
                                normalizedTitle,
                                normalizedIsbn,
                                authorId,
                                categoryId,
                                available,
                                finalStatus
                        )
                );

        applySort(
                books,
                sort
        );

        long totalElements =
                books.size();

        long start =
                (long) page * size;

        List<Book> pageBooks;

        if (start >= books.size()) {

            pageBooks =
                    new ArrayList<>();

        } else {

            int fromIndex =
                    (int) start;

            int toIndex =
                    Math.min(
                            fromIndex + size,
                            books.size()
                    );

            pageBooks =
                    books.subList(
                            fromIndex,
                            toIndex
                    );
        }

        List<BookResponse> responses =
                new ArrayList<>();

        for (Book book : pageBooks) {

            responses.add(
                    bookMapper.toResponse(book)
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

    private void applySort(
            List<Book> books,
            String sort
    ) {

        if (
                sort == null
                        || sort.isBlank()
                        || sort.equalsIgnoreCase("title")
        ) {

            books.sort(
                    Comparator.comparing(
                            Book::getTitle,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            return;
        }

        if (sort.equalsIgnoreCase("year")) {

            books.sort(
                    Comparator.comparing(
                                    Book::getPublishedYear
                            )
                            .reversed()
            );

            return;
        }

        if (sort.equalsIgnoreCase("available")) {

            books.sort(
                    Comparator.comparing(
                                    Book::getAvailableCopies
                            )
                            .reversed()
            );

            return;
        }

        throw new ValidationException(
                "Sort qiymati noto'g'ri. title, year yoki available ishlating"
        );
    }

    private String normalizeIsbn(
            String isbn
    ) {

        return isbn
                .trim()
                .replace("-", "")
                .replace(" ", "");
    }

    public BookResponse findById(
            Long id
    ) {

        Book book =
                bookRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitob topilmadi"
                                )
                        );

        return bookMapper.toResponse(book);
    }

    public BookResponse update(
            Long id,
            BookUpdateRequest request
    ) {

        Book book =
                bookRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitob topilmadi"
                                )
                        );

        String isbn =
                normalizeIsbn(
                        request.getIsbn()
                );

        if (!isbn.matches("\\d{10}|\\d{13}")) {
            throw new ValidationException(
                    "ISBN 10 yoki 13 ta raqamdan iborat bo'lishi kerak"
            );
        }

        if (
                bookRepository
                        .existsByIsbnAndIdNot(
                                isbn,
                                id
                        )
        ) {

            throw new DuplicateEntityException(
                    "Bunday ISBN bilan kitob allaqachon mavjud"
            );
        }

        int currentYear =
                Year.now().getValue();

        if (request.getPublishedYear() > currentYear) {
            throw new ValidationException(
                    "Nashr yili kelajakdagi yil bo'lishi mumkin emas"
            );
        }

        Author author =
                authorRepository
                        .findById(request.getAuthorId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Muallif topilmadi"
                                )
                        );

        Category category =
                categoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kategoriya topilmadi"
                                )
                        );

        long activeLoanCount =
                loanRepository
                        .countByBookIdAndStatus(
                                id,
                                LoanStatus.BORROWED
                        );

        if (
                request.getTotalCopies()
                        < activeLoanCount
        ) {

            throw new BusinessRuleException(
                    "Umumiy nusxalar soni faol qarzlar sonidan kam bo'lishi mumkin emas"
            );
        }

        bookMapper.updateEntity(
                book,
                request,
                author,
                category
        );

        book.setIsbn(isbn);

        book.setTitle(
                request.getTitle().trim()
        );

        book.setAvailableCopies(
                request.getTotalCopies()
                        - (int) activeLoanCount
        );

        Book updatedBook =
                bookRepository.save(book);

        return bookMapper.toResponse(
                updatedBook
        );
    }

    public void delete(
            Long id
    ) {

        Book book =
                bookRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kitob topilmadi"
                                )
                        );

        boolean hasLoanHistory =
                loanRepository.existsByBookId(id);

        if (hasLoanHistory) {

            book.setStatus(
                    BookStatus.INACTIVE
            );

            bookRepository.save(book);

        } else {

            bookRepository.delete(book);
        }
    }
}