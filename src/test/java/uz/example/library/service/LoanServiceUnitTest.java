package uz.example.library.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.example.library.dto.request.BorrowRequest;
import uz.example.library.entity.Book;
import uz.example.library.entity.Reader;
import uz.example.library.enums.BookStatus;
import uz.example.library.enums.LoanStatus;
import uz.example.library.enums.ReaderStatus;
import uz.example.library.exception.BusinessRuleException;
import uz.example.library.mapper.LoanMapper;
import uz.example.library.repository.BookRepository;
import uz.example.library.repository.LoanRepository;
import uz.example.library.repository.ReaderRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceUnitTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReaderRepository readerRepository;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanService loanService;

    @Test
    void blockedReader_shouldThrowBusinessRuleException() {

        BorrowRequest request = new BorrowRequest();
        request.setReaderId(1L);
        request.setBookId(1L);

        Reader reader = mock(Reader.class);
        Book book = mock(Book.class);

        when(readerRepository.findById(1L))
                .thenReturn(Optional.of(reader));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(reader.getStatus())
                .thenReturn(ReaderStatus.BLOCKED);

        assertThrows(
                BusinessRuleException.class,
                () -> loanService.borrowBook(request)
        );

        verifyNoInteractions(loanRepository);
    }

    @Test
    void unavailableBook_shouldThrowBusinessRuleException() {

        BorrowRequest request = new BorrowRequest();
        request.setReaderId(1L);
        request.setBookId(1L);

        Reader reader = mock(Reader.class);
        Book book = mock(Book.class);

        when(readerRepository.findById(1L))
                .thenReturn(Optional.of(reader));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(reader.getStatus())
                .thenReturn(ReaderStatus.ACTIVE);

        when(book.getStatus())
                .thenReturn(BookStatus.ACTIVE);

        when(book.getAvailableCopies())
                .thenReturn(0);

        assertThrows(
                BusinessRuleException.class,
                () -> loanService.borrowBook(request)
        );

        verifyNoInteractions(loanRepository);
    }

    @Test
    void sixthActiveLoan_shouldThrowBusinessRuleException() {

        BorrowRequest request = new BorrowRequest();
        request.setReaderId(1L);
        request.setBookId(1L);

        Reader reader = mock(Reader.class);
        Book book = mock(Book.class);

        when(readerRepository.findById(1L))
                .thenReturn(Optional.of(reader));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(reader.getStatus())
                .thenReturn(ReaderStatus.ACTIVE);

        when(reader.getId())
                .thenReturn(1L);

        when(book.getStatus())
                .thenReturn(BookStatus.ACTIVE);

        when(book.getAvailableCopies())
                .thenReturn(3);

        when(
                loanRepository.countByReaderIdAndStatus(
                        1L,
                        LoanStatus.BORROWED
                )
        ).thenReturn(5L);

        assertThrows(
                BusinessRuleException.class,
                () -> loanService.borrowBook(request)
        );

        verify(
                loanRepository,
                never()
        ).save(any());
    }
}