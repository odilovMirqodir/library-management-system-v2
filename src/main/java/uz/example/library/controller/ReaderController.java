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
import uz.example.library.dto.request.ReaderCreateRequest;
import uz.example.library.dto.request.ReaderUpdateRequest;
import uz.example.library.dto.response.ErrorResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.dto.response.ReaderResponse;
import uz.example.library.enums.ReaderStatus;
import uz.example.library.service.ReaderService;

@Tag(
        name = "Readers",
        description = "Kitobxonlarni yaratish, qidirish, yangilash, statusini o'zgartirish va o'chirish API'lari"
)
@RestController
@RequestMapping("/api/v1/readers")
public class ReaderController {

    private final ReaderService readerService;

    public ReaderController(
            ReaderService readerService
    ) {
        this.readerService = readerService;
    }

    @Operation(
            summary = "Yangi kitobxon yaratish",
            description = "Yangi kitobxonni tizimga qo'shadi. Yangi kitobxon ACTIVE holatida yaratiladi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Kitobxon muvaffaqiyatli yaratildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReaderResponse.class
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
                    description = "Bunday telefon raqami bilan kitobxon allaqachon mavjud",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ReaderResponse> create(
            @Valid @RequestBody ReaderCreateRequest request
    ) {

        ReaderResponse response =
                readerService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Kitobxonlar ro'yxatini olish",
            description = "Kitobxonlarni ism yoki telefon bo'yicha qidirish, status bo'yicha filterlash, saralash va pagination bilan qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitobxonlar ro'yxati muvaffaqiyatli olindi"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status, sort yoki pagination parametri noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping
    public PageResponse<ReaderResponse> findAll(

            @Parameter(
                    description = "Kitobxon ism-familiyasi yoki telefon raqami bo'yicha qidirish",
                    example = "Ali"
            )
            @RequestParam(required = false)
            String search,

            @Parameter(
                    description = "Kitobxon holati: ACTIVE yoki INACTIVE",
                    example = "ACTIVE"
            )
            @RequestParam(required = false)
            ReaderStatus status,

            @Parameter(
                    description = "Saralash turi: name, nameDesc, registeredAt yoki registeredAtAsc",
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

        return readerService.findAll(
                search,
                status,
                sort,
                page,
                size
        );
    }

    @Operation(
            summary = "Kitobxonni ID bo'yicha olish",
            description = "Berilgan ID bo'yicha bitta kitobxon ma'lumotlarini qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitobxon topildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReaderResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kitobxon topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ReaderResponse findById(

            @Parameter(
                    description = "Kitobxon ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        return readerService.findById(id);
    }

    @Operation(
            summary = "Kitobxonni yangilash",
            description = "Kitobxonning ism-familiyasi, telefon raqami va email ma'lumotlarini yangilaydi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitobxon muvaffaqiyatli yangilandi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReaderResponse.class
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
                    description = "Kitobxon topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bunday telefon raqami boshqa kitobxonda mavjud",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public ReaderResponse update(

            @Parameter(
                    description = "Yangilanadigan kitobxon ID",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            ReaderUpdateRequest request
    ) {

        return readerService.update(
                id,
                request
        );
    }

    @Operation(
            summary = "Kitobxon statusini o'zgartirish",
            description = "Kitobxon statusini ACTIVE yoki INACTIVE holatiga o'zgartiradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitobxon statusi muvaffaqiyatli o'zgartirildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReaderResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status qiymati noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kitobxon topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{id}/status")
    public ReaderResponse changeStatus(

            @Parameter(
                    description = "Kitobxon ID",
                    example = "1"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Yangi status: ACTIVE yoki INACTIVE",
                    example = "INACTIVE"
            )
            @RequestParam ReaderStatus status
    ) {

        return readerService.changeStatus(
                id,
                status
        );
    }

    @Operation(
            summary = "Kitobxonni o'chirish",
            description = "Faol qarzi bo'lsa o'chirish taqiqlanadi. " +
                    "Qarz tarixi bo'lsa INACTIVE holatiga o'tkaziladi, " +
                    "qarz tarixi bo'lmasa fizik o'chiriladi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Kitobxon muvaffaqiyatli o'chirildi yoki INACTIVE holatiga o'tkazildi"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kitobxon topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Kitobxonning faol qarzi mavjud",
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
                    description = "O'chiriladigan kitobxon ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        readerService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}