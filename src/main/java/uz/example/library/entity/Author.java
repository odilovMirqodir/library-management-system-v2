package uz.example.library.entity;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 100)
    private String country;

    @Column(length = 1000)
    private String biography;

    public Author() {

    }

    public Author(String fullName, LocalDate birthDate, String country, String biography) {
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
