package uz.example.library.dto.response;

import java.time.LocalDate;


public class AuthorResponse {
    private Long id;
    private String fullName;
    private LocalDate birthDate;
    private String country;
    private String biography;

    public AuthorResponse(Long id, String fullName, LocalDate birthDate, String country, String biography) {
        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.country = country;
        this.biography = biography;
    }
    public Long getId() {
        return id;
    }

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
}
