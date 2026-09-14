package uz.example.library.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Kitob ma'lumotlarini yangilash uchun request"
)
public class BookUpdateRequest {

    @Schema(
            description = "Kitob ISBN raqami",
            example = "9780134685991"
    )
    @NotBlank(message = "ISBN bo'sh bo'lishi mumkin emas")
    private String isbn;

    @Schema(
            description = "Kitob nomi",
            example = "Effective Java"
    )
    @NotBlank(message = "Kitob nomi bo'sh bo'lishi mumkin emas")
    @Size(
            min = 2,
            max = 200,
            message = "Kitob nomi 2 tadan 200 tagacha belgidan iborat bo'lishi kerak"
    )
    private String title;

    @Schema(
            description = "Muallif ID",
            example = "1"
    )
    @NotNull(message = "Muallif ID si bo'sh bo'lishi mumkin emas")
    private Long authorId;

    @Schema(
            description = "Kategoriya ID",
            example = "1"
    )
    @NotNull(message = "Kategoriya ID si bo'sh bo'lishi mumkin emas")
    private Long categoryId;

    @Schema(
            description = "Kitob nashr qilingan yil",
            example = "2018"
    )
    @NotNull(message = "Nashr yili bo'sh bo'lishi mumkin emas")
    @Min(
            value = 1000,
            message = "Nashr yili 1000 dan kichik bo'lishi mumkin emas"
    )
    private Integer publishedYear;

    @Schema(
            description = "Kitobning jami nusxalari soni. Faol qarzlar sonidan kam bo'lishi mumkin emas",
            example = "5"
    )
    @NotNull(message = "Kitob nusxalari soni bo'sh bo'lishi mumkin emas")
    @Min(
            value = 1,
            message = "Kitob nusxalari soni kamida 1 bo'lishi kerak"
    )
    private Integer totalCopies;

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(Integer publishedYear) {
        this.publishedYear = publishedYear;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }
}