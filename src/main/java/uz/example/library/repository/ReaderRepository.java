package uz.example.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.example.library.entity.Reader;
import uz.example.library.enums.ReaderStatus;

import java.util.List;

public interface ReaderRepository extends JpaRepository<Reader, Long> {

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    );

    List<Reader> findByFullNameContainingIgnoreCaseOrPhoneContaining(
            String fullName,
            String phone
    );

    List<Reader> findByStatus(
            ReaderStatus status
    );

    List<Reader> findByStatusAndFullNameContainingIgnoreCaseOrStatusAndPhoneContaining(
            ReaderStatus status1,
            String fullName,
            ReaderStatus status2,
            String phone
    );
}