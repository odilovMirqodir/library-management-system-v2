package uz.example.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.example.library.dto.request.CategoryCreateRequest;
import uz.example.library.dto.request.CategoryUpdateRequest;
import uz.example.library.dto.response.CategoryResponse;
import uz.example.library.dto.response.ErrorResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.service.CategoryService;

@Tag(
        name = "Categories",
        description = "Kitob kategoriyalarini yaratish, ko'rish, yangilash va o'chirish API'lari"
)
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @Operation(
            summary = "Yangi kategoriya yaratish",
            description = "Yangi kitob kategoriyasini tizimga qo'shadi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Kategoriya muvaffaqiyatli yaratildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kiritilgan ma'lumotlar noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bunday nomli kategoriya allaqachon mavjud",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {

        CategoryResponse response =
                categoryService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Kategoriyalar ro'yxatini olish",
            description = "Kategoriyalarni saralash va pagination bilan qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kategoriyalar ro'yxati muvaffaqiyatli olindi"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Pagination yoki sort parametri noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping
    public PageResponse<CategoryResponse> findAll(

            @Parameter(
                    description = "Saralash turi: name yoki nameDesc",
                    example = "name"
            )
            @RequestParam(defaultValue = "name")
            String sort,

            @Parameter(
                    description = "Sahifa raqami. 0 dan boshlanadi",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Bir sahifadagi elementlar soni. Maksimum 100",
                    example = "10"
            )
            @RequestParam(defaultValue = "10")
            int size
    ) {

        return categoryService.findAll(
                sort,
                page,
                size
        );
    }

    @Operation(
            summary = "Kategoriyani ID bo'yicha olish",
            description = "Berilgan ID bo'yicha bitta kategoriyani qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kategoriya topildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kategoriya topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public CategoryResponse findById(

            @Parameter(
                    description = "Kategoriya ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        return categoryService.findById(id);
    }

    @Operation(
            summary = "Kategoriyani yangilash",
            description = "Berilgan ID bo'yicha kategoriya ma'lumotlarini yangilaydi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kategoriya muvaffaqiyatli yangilandi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kiritilgan ma'lumotlar noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kategoriya topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bunday nomli boshqa kategoriya allaqachon mavjud",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public CategoryResponse update(

            @Parameter(
                    description = "Yangilanadigan kategoriya ID",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            CategoryUpdateRequest request
    ) {

        return categoryService.update(
                id,
                request
        );
    }

    @Operation(
            summary = "Kategoriyani o'chirish",
            description = "Kategoriyaga bog'langan kitob mavjud bo'lmasa uni o'chiradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Kategoriya muvaffaqiyatli o'chirildi"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kategoriya topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Kategoriyaga kitob bog'langanligi sababli o'chirib bo'lmaydi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(

            @Parameter(
                    description = "O'chiriladigan kategoriya ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        categoryService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}