package uz.example.library.dto.response;

import uz.example.library.enums.ReaderStatus;

import java.time.LocalDateTime;

public class ReaderResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private LocalDateTime registeredAt;
    private ReaderStatus status;

    public ReaderResponse(
            Long id,
            String fullName,
            String phone,
            String email,
            LocalDateTime registeredAt,
            ReaderStatus status
    ) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public ReaderStatus getStatus() {
        return status;
    }
}