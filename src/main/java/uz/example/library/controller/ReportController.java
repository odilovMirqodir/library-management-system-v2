package uz.example.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.example.library.dto.response.LibraryReportResponse;
import uz.example.library.dto.response.ErrorResponse;
import uz.example.library.service.ReportService;

@Tag(
        name = "Reports",
        description = "Kutubxona bo'yicha umumiy statistik hisobot API'lari"
)
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(
            ReportService reportService
    ) {
        this.reportService = reportService;
    }

    @Operation(
            summary = "Kutubxona umumiy hisobotini olish",
            description = "Kutubxonadagi kitob nomlari soni, jami nusxalar, mavjud nusxalar, " +
                    "qarzdagi kitoblar, faol kitobxonlar va kechikkan qarzlar sonini qaytaradi."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Hisobot muvaffaqiyatli olindi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = LibraryReportResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Serverda kutilmagan xatolik yuz berdi",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/summary")
    public LibraryReportResponse getSummary() {

        return reportService.getSummary();
    }
}