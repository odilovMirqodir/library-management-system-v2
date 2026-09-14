package uz.example.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.example.library.entity.Author;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findAllByOrderByFullNameAsc();

    List<Author> findByFullNameContainingIgnoreCaseOrderByFullNameAsc(String fullName);
}