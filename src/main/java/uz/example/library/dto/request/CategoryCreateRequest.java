package uz.example.library.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Yangi kategoriya yaratish uchun request"
)
public class CategoryCreateRequest {

    @Schema(
            description = "Kategoriya nomi",
            example = "Programming"
    )
    @NotBlank(message = "Kategoriya nomi bo'sh bo'lishi mumkin emas")
    @Size(
            min = 2,
            max = 80,
            message = "Kategoriya nomi 2 tadan 80 tagacha belgidan iborat bo'lishi kerak"
    )
    private String name;

    @Schema(
            description = "Kategoriya haqida qisqacha tavsif",
            example = "Dasturlash va software development bo'yicha kitoblar"
    )
    @Size(
            max = 255,
            message = "Tavsif 255 ta belgidan oshmasligi kerak"
    )
    private String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}