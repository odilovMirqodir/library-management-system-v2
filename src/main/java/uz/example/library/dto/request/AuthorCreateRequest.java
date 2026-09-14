package uz.example.library.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(
        description = "Yangi muallif yaratish uchun request"
)
public class AuthorCreateRequest {

    @Schema(
            description = "Muallifning to'liq ism-familiyasi",
            example = "George Orwell"
    )
    @NotBlank(message = "Muallif ismi bo'sh bo'lishi mumkin emas")
    @Size(
            min = 2,
            max = 120,
            message = "Muallif ismi 2 dan 120 tagacha belgidan iborat bo'lishi kerak"
    )
    private String fullName;

    @Schema(
            description = "Muallifning tug'ilgan sanasi. Kelajakdagi sana bo'lishi mumkin emas",
            example = "1903-06-25"
    )
    @PastOrPresent(
            message = "Tug'ilgan sana kelajakdagi sana bo'lishi mumkin emas"
    )
    private LocalDate birthDate;

    @Schema(
            description = "Muallif davlati",
            example = "United Kingdom"
    )
    @Size(
            max = 100,
            message = "Davlat nomi 100 ta belgidan oshmasligi kerak"
    )
    private String country;

    @Schema(
            description = "Muallif haqida qisqacha biografiya",
            example = "English novelist, essayist and journalist."
    )
    @Size(
            max = 1000,
            message = "Biografiya 1000 ta belgidan oshmasligi kerak"
    )
    private String biography;

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getCountry() {
        return country;
    }

    public String getBiography() {
        return biography;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }
}