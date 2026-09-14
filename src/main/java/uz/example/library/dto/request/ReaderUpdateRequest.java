package uz.example.library.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Kitobxon ma'lumotlarini yangilash uchun request"
)
public class ReaderUpdateRequest {

    @Schema(
            description = "Kitobxonning to'liq ism-familiyasi",
            example = "Ali Valiyev"
    )
    @NotBlank(message = "Kitobxon ismi bo'sh bo'lishi mumkin emas")
    @Size(
            min = 2,
            max = 120,
            message = "Kitobxon ismi 2 tadan 120 tagacha belgidan iborat bo'lishi kerak"
    )
    private String fullName;

    @Schema(
            description = "Kitobxon telefon raqami. +998 format tavsiya etiladi",
            example = "+998901234567"
    )
    @NotBlank(message = "Telefon raqami bo'sh bo'lishi mumkin emas")
    private String phone;

    @Schema(
            description = "Kitobxon email manzili",
            example = "ali@example.com"
    )
    @Email(message = "Email formati noto'g'ri")
    private String email;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}