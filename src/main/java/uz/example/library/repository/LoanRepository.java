package uz.example.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.example.library.entity.Loan;
import uz.example.library.enums.LoanStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByReaderIdAndStatus(
            Long readerId,
            LoanStatus status
    );

    boolean existsByReaderIdAndBookIsbnAndStatus(
            Long readerId,
            String isbn,
            LoanStatus status
    );

    long countByBookIdAndStatus(
            Long bookId,
            LoanStatus status
    );

    boolean existsByBookId(Long bookId);

    boolean existsByReaderId(Long readerId);

    List<Loan> findByStatus(
            LoanStatus status
    );

    List<Loan> findByReaderId(
            Long readerId
    );

    List<Loan> findByBookId(
            Long bookId
    );

    List<Loan> findByStatusAndReturnedAtIsNullAndDueDateBeforeOrderByDueDateAsc(
            LoanStatus status,
            LocalDateTime now
    );

    @Query("""
            SELECT l
            FROM Loan l
            WHERE (:status IS NULL OR l.status = :status)
              AND (:readerId IS NULL OR l.reader.id = :readerId)
              AND (:bookId IS NULL OR l.book.id = :bookId)
            ORDER BY l.borrowedAt DESC
            """)
    List<Loan> findWithFilters(
            @Param("status") LoanStatus status,
            @Param("readerId") Long readerId,
            @Param("bookId") Long bookId
    );
}