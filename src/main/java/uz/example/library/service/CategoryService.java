package uz.example.library.service;

import org.springframework.stereotype.Service;
import uz.example.library.dto.request.CategoryCreateRequest;
import uz.example.library.dto.request.CategoryUpdateRequest;
import uz.example.library.dto.response.CategoryResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.entity.Category;
import uz.example.library.exception.BusinessRuleException;
import uz.example.library.exception.DuplicateEntityException;
import uz.example.library.exception.EntityNotFoundException;
import uz.example.library.exception.ValidationException;
import uz.example.library.mapper.CategoryMapper;
import uz.example.library.repository.BookRepository;
import uz.example.library.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final BookRepository bookRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            BookRepository bookRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.bookRepository = bookRepository;
    }

    public CategoryResponse create(
            CategoryCreateRequest request
    ) {

        String name =
                normalizeName(
                        request.getName()
                );

        if (
                categoryRepository
                        .existsByNameIgnoreCase(name)
        ) {

            throw new DuplicateEntityException(
                    "Bunday kategoriya nomi allaqachon mavjud"
            );
        }

        Category category =
                categoryMapper.toEntity(request);

        category.setName(name);

        if (request.getDescription() != null) {
            category.setDescription(
                    normalizeOptionalText(
                            request.getDescription(),
                            "Kategoriya tavsifi"
                    )
            );
        }

        Category savedCategory =
                categoryRepository.save(category);

        return categoryMapper.toResponse(
                savedCategory
        );
    }

    public PageResponse<CategoryResponse> findAll(
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

        List<Category> categories =
                new ArrayList<>(
                        categoryRepository
                                .findAllByOrderByNameAsc()
                );

        applySort(
                categories,
                sort
        );

        long totalElements =
                categories.size();

        long start =
                (long) page * size;

        List<Category> pageCategories;

        if (start >= categories.size()) {

            pageCategories =
                    new ArrayList<>();

        } else {

            int fromIndex =
                    (int) start;

            int toIndex =
                    Math.min(
                            fromIndex + size,
                            categories.size()
                    );

            pageCategories =
                    categories.subList(
                            fromIndex,
                            toIndex
                    );
        }

        List<CategoryResponse> responses =
                new ArrayList<>();

        for (Category category : pageCategories) {

            responses.add(
                    categoryMapper.toResponse(category)
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
            List<Category> categories,
            String sort
    ) {

        if (
                sort == null
                        || sort.isBlank()
                        || sort.equalsIgnoreCase("name")
                        || sort.equalsIgnoreCase("nameAsc")
        ) {

            categories.sort(
                    Comparator.comparing(
                            Category::getName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            return;
        }

        if (
                sort.equalsIgnoreCase("nameDesc")
        ) {

            categories.sort(
                    Comparator.comparing(
                                    Category::getName,
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

    public CategoryResponse findById(
            Long id
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kategoriya topilmadi"
                                )
                        );

        return categoryMapper.toResponse(
                category
        );
    }

    public CategoryResponse update(
            Long id,
            CategoryUpdateRequest request
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kategoriya topilmadi"
                                )
                        );

        String name =
                normalizeName(
                        request.getName()
                );

        if (
                categoryRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                name,
                                id
                        )
        ) {

            throw new DuplicateEntityException(
                    "Bunday kategoriya nomi allaqachon mavjud"
            );
        }

        categoryMapper.updateEntity(
                category,
                request
        );

        category.setName(name);

        if (request.getDescription() != null) {
            category.setDescription(
                    normalizeOptionalText(
                            request.getDescription(),
                            "Kategoriya tavsifi"
                    )
            );
        }

        Category updatedCategory =
                categoryRepository.save(category);

        return categoryMapper.toResponse(
                updatedCategory
        );
    }

    public void delete(
            Long id
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Kategoriya topilmadi"
                                )
                        );

        if (
                bookRepository
                        .existsByCategoryId(id)
        ) {

            throw new BusinessRuleException(
                    "Bu kategoriya kitobda ishlatilgan, o'chirib bo'lmaydi"
            );
        }

        categoryRepository.delete(category);
    }

    private String normalizeName(
            String name
    ) {

        String normalized =
                name.trim();

        if (
                normalized.length() < 2
                        || normalized.length() > 80
        ) {

            throw new ValidationException(
                    "Kategoriya nomi 2 tadan 80 tagacha belgidan iborat bo'lishi kerak"
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