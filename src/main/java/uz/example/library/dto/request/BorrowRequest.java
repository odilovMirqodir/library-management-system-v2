package uz.example.library.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = "Kitobni kitobxonga qarzga berish uchun request"
)
public class BorrowRequest {

    @Schema(
            description = "Kitobxon ID",
            example = "1"
    )
    @NotNull(message = "Kitobxon ID si bo'sh bo'lishi mumkin emas")
    private Long readerId;

    @Schema(
            description = "Kitob ID",
            example = "1"
    )
    @NotNull(message = "Kitob ID si bo'sh bo'lishi mumkin emas")
    private Long bookId;

    public Long getReaderId() {
        return readerId;
    }

    public void setReaderId(Long readerId) {
        this.readerId = readerId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}