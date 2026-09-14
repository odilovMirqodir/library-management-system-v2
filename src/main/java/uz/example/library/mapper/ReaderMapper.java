package uz.example.library.mapper;

import org.springframework.stereotype.Component;
import uz.example.library.dto.request.ReaderCreateRequest;
import uz.example.library.dto.response.ReaderResponse;
import uz.example.library.entity.Reader;
import uz.example.library.dto.request.ReaderUpdateRequest;

@Component
public class ReaderMapper {

    public Reader toEntity(ReaderCreateRequest request) {
        Reader reader = new Reader();

        reader.setFullName(request.getFullName());
        reader.setPhone(request.getPhone());
        reader.setEmail(request.getEmail());

        return reader;
    }

    public ReaderResponse toResponse(Reader reader) {
        return new ReaderResponse(
                reader.getId(),
                reader.getFullName(),
                reader.getPhone(),
                reader.getEmail(),
                reader.getRegisteredAt(),
                reader.getStatus()
        );
    }

    public void updateEntity(
            Reader reader,
            ReaderUpdateRequest request
    ) {
        reader.setFullName(request.getFullName());
        reader.setPhone(request.getPhone());
        reader.setEmail(request.getEmail());
    }
}