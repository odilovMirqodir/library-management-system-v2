package uz.example.library.mapper;

import org.springframework.stereotype.Component;
import uz.example.library.dto.request.CategoryCreateRequest;
import uz.example.library.dto.request.CategoryUpdateRequest;
import uz.example.library.dto.response.CategoryResponse;
import uz.example.library.entity.Category;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryCreateRequest request) {
        return new Category(
                request.getName(),
                request.getDescription()
        );
    }

    public void updateEntity(
            Category category,
            CategoryUpdateRequest request
    ) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}