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
import uz.example.library.dto.request.BorrowRequest;
import uz.example.library.dto.response.ErrorResponse;
import uz.example.library.dto.response.LoanResponse;
import uz.example.library.dto.response.OverdueLoanResponse;
import uz.example.library.dto.response.PageResponse;
import uz.example.library.enums.LoanStatus;
import uz.example.library.service.LoanService;

@Tag(
        name = "Loans",
        description = "Kitob berish, qaytarish, qarzlarni filterlash va kechikkan qarzlarni ko'rish API'lari"
)
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(
            LoanService loanService
    ) {
        this.loanService = loanService;
    }

    @Operation(
            summary = "Kitobni kitobxonga berish",
            description = "ACTIVE kitobxon ACTIVE kitobni qarzga oladi. " +
                    "Kitob mavjud bo'lishi, availableCopies 0 dan katta bo'lishi, " +
                    "kitobxonning faol qarzlari 5 tadan kam bo'lishi va ayni ISBN faol qarzda bo'lmasligi kerak. " +
                    "Qarz muddati avtomatik 14 kun qilib belgilanadi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Kitob muvaffaqiyatli qarzga berildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = LoanResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request ma'lumotlari noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Kitob yoki kitobxon topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Kitob berish biznes qoidalaridan biri buzildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<LoanResponse> borrowBook(
            @Valid @RequestBody BorrowRequest request
    ) {

        LoanResponse response =
                loanService.borrowBook(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Kitobni qaytarish",
            description = "BORROWED holatidagi qarzni qaytaradi. " +
                    "returnedAt joriy vaqtga o'rnatiladi, status RETURNED bo'ladi " +
                    "va kitobning availableCopies qiymati 1 taga oshadi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kitob muvaffaqiyatli qaytarildi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = LoanResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Qarz topilmadi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Kitob allaqachon qaytarilgan yoki nusxalar holati noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/{id}/return")
    public LoanResponse returnBook(

            @Parameter(
                    description = "Qaytariladigan qarz ID",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        return loanService.returnBook(id);
    }

    @Operation(
            summary = "Qarzlar ro'yxatini olish",
            description = "Qarzlarni status, kitobxon va kitob bo'yicha filterlash, " +
                    "saralash va pagination bilan qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Qarzlar ro'yxati muvaffaqiyatli olindi"
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
    public PageResponse<LoanResponse> findAll(

            @Parameter(
                    description = "Qarz holati: BORROWED yoki RETURNED",
                    example = "BORROWED"
            )
            @RequestParam(required = false)
            LoanStatus status,

            @Parameter(
                    description = "Kitobxon ID bo'yicha filter",
                    example = "1"
            )
            @RequestParam(required = false)
            Long readerId,

            @Parameter(
                    description = "Kitob ID bo'yicha filter",
                    example = "1"
            )
            @RequestParam(required = false)
            Long bookId,

            @Parameter(
                    description = "Saralash turi: borrowedAt, borrowedAtAsc, dueDate yoki dueDateDesc",
                    example = "borrowedAt"
            )
            @RequestParam(defaultValue = "borrowedAt")
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

        return loanService.findAll(
                status,
                readerId,
                bookId,
                sort,
                page,
                size
        );
    }

    @Operation(
            summary = "Kechikkan qarzlarni olish",
            description = "dueDate joriy vaqtdan oldin bo'lgan, hali qaytarilmagan BORROWED qarzlarni qaytaradi. " +
                    "Default holatda eng ko'p kechikkan qarz birinchi chiqadi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Kechikkan qarzlar ro'yxati muvaffaqiyatli olindi"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Sort yoki pagination parametri noto'g'ri",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/overdue")
    public PageResponse<OverdueLoanResponse> findOverdue(

            @Parameter(
                    description = "Saralash turi: dueDate yoki dueDateDesc. dueDate eng ko'p kechikkanidan boshlaydi",
                    example = "dueDate"
            )
            @RequestParam(defaultValue = "dueDate")
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

        return loanService.findOverdue(
                sort,
                page,
                size
        );
    }
}