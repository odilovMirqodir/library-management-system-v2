package uz.example.library.entity;

import jakarta.persistence.*;
import uz.example.library.enums.ReaderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "readers")
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phone;

    private String email;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReaderStatus status;

    public Reader() {
    }

    public Reader(
            String fullName,
            String phone,
            String email,
            LocalDateTime registeredAt,
            ReaderStatus status
    ) {
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

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public void setStatus(ReaderStatus status) {
        this.status = status;
    }
}