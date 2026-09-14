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
import uz.example.library.dto.request.AuthorCreateRequest;
import uz.example.library.dto.request.AuthorUpdateRequest;
import uz.example.library.dto.response.AuthorResponse;
import uz.example.library.dto.response.ErrorResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.service.AuthorService;

@Tag(
        name = "Authors",
        description = "Mualliflarni yaratish, qidirish, yangilash va o'chirish API'lari"
)
@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(
            AuthorService authorService
    ) {
        this.authorService = authorService;
    }

    @Operation(
            summary = "Yangi muallif yaratish",
            description = "Yangi muallifni tizimga qo'shadi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Muallif muvaffaqiyatli yaratildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthorResponse.class
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
            )
    })
    @PostMapping
    public ResponseEntity<AuthorResponse> create(
            @Valid @RequestBody AuthorCreateRequest request
    ) {

        AuthorResponse response =
                authorService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Mualliflar ro'yxatini olish",
            description = "Mualliflarni qidirish, saralash va pagination bilan qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mualliflar ro'yxati muvaffaqiyatli olindi"
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
    public PageResponse<AuthorResponse> findAll(

            @Parameter(
                    description = "Muallif ism-familiyasi bo'yicha qidirish",
                    example = "George Orwell"
            )
            @RequestParam(required = false)
            String search,

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

        return authorService.findAll(
                search,
                sort,
                page,
                size
        );
    }

    @Operation(
            summary = "Muallifni ID bo'yicha olish",
            description = "Berilgan ID bo'yicha bitta muallifni qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muallif topildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Muallif topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public AuthorResponse findById(

            @Parameter(
                    description = "Muallif ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        return authorService.findById(id);
    }

    @Operation(
            summary = "Muallifni yangilash",
            description = "Berilgan ID bo'yicha muallif ma'lumotlarini yangilaydi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muallif muvaffaqiyatli yangilandi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthorResponse.class
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
                    description = "Muallif topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public AuthorResponse update(

            @Parameter(
                    description = "Yangilanadigan muallif ID",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            AuthorUpdateRequest request
    ) {

        return authorService.update(
                id,
                request
        );
    }

    @Operation(
            summary = "Muallifni o'chirish",
            description = "Muallifga bog'langan kitob mavjud bo'lmasa uni o'chiradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Muallif muvaffaqiyatli o'chirildi"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Muallif topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Muallifga kitob bog'langanligi sababli o'chirib bo'lmaydi",
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
                    description = "O'chiriladigan muallif ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        authorService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}