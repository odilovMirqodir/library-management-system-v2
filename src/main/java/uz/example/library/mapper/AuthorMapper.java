package uz.example.library.mapper;

import org.springframework.stereotype.Component;
import uz.example.library.dto.request.AuthorCreateRequest;
import uz.example.library.dto.response.AuthorResponse;
import uz.example.library.entity.Author;
import uz.example.library.dto.request.AuthorUpdateRequest;

@Component
public class AuthorMapper {
    public Author toEntity(AuthorCreateRequest request) {
        return new Author(
                request.getFullName(),
                request.getBirthDate(),
                request.getCountry(),
                request.getBiography()
        );
    }

    public AuthorResponse toResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getFullName(),
                author.getBirthDate(),
                author.getCountry(),
                author.getBiography()
        );
    }

    public void updateEntity(Author author, AuthorUpdateRequest request) {
        author.setFullName(request.getFullName());
        author.setBirthDate(request.getBirthDate());
        author.setCountry(request.getCountry());
        author.setBiography(request.getBiography());
    }
}
