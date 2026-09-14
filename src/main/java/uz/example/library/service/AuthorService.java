package uz.example.library.service;

import org.springframework.stereotype.Service;
import uz.example.library.dto.request.AuthorCreateRequest;
import uz.example.library.dto.request.AuthorUpdateRequest;
import uz.example.library.dto.response.AuthorResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.entity.Author;
import uz.example.library.exception.BusinessRuleException;
import uz.example.library.exception.EntityNotFoundException;
import uz.example.library.exception.ValidationException;
import uz.example.library.mapper.AuthorMapper;
import uz.example.library.repository.AuthorRepository;
import uz.example.library.repository.BookRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookRepository bookRepository;

    public AuthorService(
            AuthorRepository authorRepository,
            AuthorMapper authorMapper,
            BookRepository bookRepository
    ) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
        this.bookRepository = bookRepository;
    }

    public AuthorResponse create(
            AuthorCreateRequest request
    ) {

        String fullName =
                normalizeFullName(
                        request.getFullName()
                );

        Author author =
                authorMapper.toEntity(request);

        author.setFullName(fullName);

        if (request.getCountry() != null) {
            author.setCountry(
                    normalizeOptionalText(
                            request.getCountry(),
                            "Davlat nomi"
                    )
            );
        }

        if (request.getBiography() != null) {
            author.setBiography(
                    normalizeOptionalText(
                            request.getBiography(),
                            "Biografiya"
                    )
            );
        }

        Author savedAuthor =
                authorRepository.save(author);

        return authorMapper.toResponse(
                savedAuthor
        );
    }

    public PageResponse<AuthorResponse> findAll(
            String search,
            String sort,
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

        List<Author> authors;

        if (search == null || search.isBlank()) {

            authors = new ArrayList<>(
                    authorRepository
                            .findAllByOrderByFullNameAsc()
            );

        } else {

            authors = new ArrayList<>(
                    authorRepository
                            .findByFullNameContainingIgnoreCaseOrderByFullNameAsc(
                                    search.trim()
                            )
            );
        }

        applySort(
                authors,
                sort
        );

        long totalElements =
                authors.size();

        long start =
                (long) page * size;

        List<Author> pageAuthors;

        if (start >= authors.size()) {

            pageAuthors =
                    new ArrayList<>();

        } else {

            int fromIndex =
                    (int) start;

            int toIndex =
                    Math.min(
                            fromIndex + size,
                            authors.size()
                    );

            pageAuthors =
                    authors.subList(
                            fromIndex,
                            toIndex
                    );
        }

        List<AuthorResponse> responses =
                new ArrayList<>();

        for (Author author : pageAuthors) {

            responses.add(
                    authorMapper.toResponse(author)
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

    private void applySort(
            List<Author> authors,
            String sort
    ) {

        if (
                sort == null
                        || sort.isBlank()
                        || sort.equalsIgnoreCase("name")
                        || sort.equalsIgnoreCase("fullName")
                        || sort.equalsIgnoreCase("nameAsc")
                        || sort.equalsIgnoreCase("fullNameAsc")
        ) {

            authors.sort(
                    Comparator.comparing(
                            Author::getFullName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("nameDesc")
                        || sort.equalsIgnoreCase("fullNameDesc")
        ) {

            authors.sort(
                    Comparator.comparing(
                                    Author::getFullName,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                            .reversed()
            );

            return;
        }

        throw new ValidationException(
                "Sort qiymati noto'g'ri. name yoki nameDesc ishlating"
        );
    }

    public AuthorResponse findById(
            Long id
    ) {

        Author author =
                authorRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Muallif topilmadi"
                                )
                        );

        return authorMapper.toResponse(author);
    }

    public AuthorResponse update(
            Long id,
            AuthorUpdateRequest request
    ) {

        Author author =
                authorRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Muallif topilmadi"
                                )
                        );

        String fullName =
                normalizeFullName(
                        request.getFullName()
                );

        authorMapper.updateEntity(
                author,
                request
        );

        author.setFullName(fullName);

        if (request.getCountry() != null) {
            author.setCountry(
                    normalizeOptionalText(
                            request.getCountry(),
                            "Davlat nomi"
                    )
            );
        }

        if (request.getBiography() != null) {
            author.setBiography(
                    normalizeOptionalText(
                            request.getBiography(),
                            "Biografiya"
                    )
            );
        }

        Author updatedAuthor =
                authorRepository.save(author);

        return authorMapper.toResponse(
                updatedAuthor
        );
    }

    public void delete(
            Long id
    ) {

        Author author =
                authorRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Muallif topilmadi"
                                )
                        );

        if (bookRepository.existsByAuthorId(id)) {

            throw new BusinessRuleException(
                    "Bu muallifga kitob bog'langan, o'chirib bo'lmaydi"
            );
        }

        authorRepository.delete(author);
    }

    private String normalizeFullName(
            String fullName
    ) {

        String normalized =
                fullName.trim();

        if (
                normalized.length() < 2
                        || normalized.length() > 120
        ) {

            throw new ValidationException(
                    "Muallif ismi 2 dan 120 tagacha belgidan iborat bo'lishi kerak"
            );
        }

        return normalized;
    }

    private String normalizeOptionalText(
            String value,
            String fieldName
    ) {

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {

            throw new ValidationException(
                    fieldName + " bo'sh bo'lishi mumkin emas"
            );
        }

        return normalized;
    }
}