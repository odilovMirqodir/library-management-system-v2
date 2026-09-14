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
import uz.example.library.dto.request.BookCreateRequest;
import uz.example.library.dto.request.BookUpdateRequest;
import uz.example.library.dto.response.BookResponse;
import uz.example.library.dto.response.ErrorResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.enums.BookStatus;
import uz.example.library.service.BookService;

@Tag(
        name = "Books",
        description = "Kitoblarni yaratish, qidirish, filterlash, yangilash va o'chirish API'lari"
)
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(
            BookService bookService
    ) {
        this.bookService = bookService;
    }

    @Operation(
            summary = "Yangi kitob yaratish",
            description = "Yangi kitobni mavjud muallif va kategoriyaga bog'lab tizimga qo'shadi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Kitob muvaffaqiyatli yaratildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BookResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ISBN, nashr yili yoki boshqa ma'lumotlar noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Muallif yoki kategoriya topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bunday ISBN bilan kitob allaqachon mavjud",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody BookCreateRequest request
    ) {

        BookResponse response =
                bookService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Kitoblar ro'yxatini olish",
            description = "Kitoblarni bir nechta filter, saralash va pagination orqali qaytaradi. " +
                    "Status berilmasa faqat ACTIVE kitoblar qaytariladi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitoblar ro'yxati muvaffaqiyatli olindi"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filter, sort yoki pagination parametri noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping
    public PageResponse<BookResponse> findAll(

            @Parameter(
                    description = "Kitob nomi bo'yicha qisman qidirish",
                    example = "Spring"
            )
            @RequestParam(required = false)
            String title,

            @Parameter(
                    description = "ISBN bo'yicha aniq qidirish. Bo'sh joy va '-' belgisi normalizatsiya qilinadi",
                    example = "978-0134685991"
            )
            @RequestParam(required = false)
            String isbn,

            @Parameter(
                    description = "Muallif ID bo'yicha filter",
                    example = "1"
            )
            @RequestParam(required = false)
            Long authorId,

            @Parameter(
                    description = "Kategoriya ID bo'yicha filter",
                    example = "2"
            )
            @RequestParam(required = false)
            Long categoryId,

            @Parameter(
                    description = "Mavjud nusxasi bor yoki yo'qligi bo'yicha filter",
                    example = "true"
            )
            @RequestParam(required = false)
            Boolean available,

            @Parameter(
                    description = "Kitob holati: ACTIVE yoki INACTIVE",
                    example = "ACTIVE"
            )
            @RequestParam(required = false)
            BookStatus status,

            @Parameter(
                    description = "Saralash turi: title, year yoki available",
                    example = "title"
            )
            @RequestParam(defaultValue = "title")
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

        return bookService.findAll(
                title,
                isbn,
                authorId,
                categoryId,
                available,
                status,
                sort,
                page,
                size
        );
    }

    @Operation(
            summary = "Kitobni ID bo'yicha olish",
            description = "Berilgan ID bo'yicha kitob va uning muallif/kategoriya ma'lumotlarini qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitob topildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BookResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kitob topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public BookResponse findById(

            @Parameter(
                    description = "Kitob ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        return bookService.findById(id);
    }

    @Operation(
            summary = "Kitobni yangilash",
            description = "Kitob ma'lumotlarini yangilaydi. " +
                    "totalCopies faol qarzlar sonidan kam bo'lishi mumkin emas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitob muvaffaqiyatli yangilandi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BookResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ISBN, nashr yili yoki boshqa ma'lumotlar noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kitob, muallif yoki kategoriya topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "ISBN band yoki totalCopies biznes qoidasiga zid",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public BookResponse update(

            @Parameter(
                    description = "Yangilanadigan kitob ID",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            BookUpdateRequest request
    ) {

        return bookService.update(
                id,
                request
        );
    }

    @Operation(
            summary = "Kitobni o'chirish",
            description = "Agar kitobda qarz tarixi bo'lmasa fizik o'chiriladi. " +
                    "Qarz tarixi mavjud bo'lsa kitob INACTIVE holatiga o'tkaziladi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Kitob muvaffaqiyatli o'chirildi yoki INACTIVE holatiga o'tkazildi"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kitob topilmadi",
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
                    description = "O'chiriladigan kitob ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        bookService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}