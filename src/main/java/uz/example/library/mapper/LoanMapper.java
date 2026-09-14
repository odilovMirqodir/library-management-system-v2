package uz.example.library.mapper;

import org.springframework.stereotype.Component;
import uz.example.library.dto.response.LoanResponse;
import uz.example.library.entity.Loan;

@Component
public class LoanMapper {

    public LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getBook().getIsbn(),
                loan.getReader().getId(),
                loan.getReader().getFullName(),
                loan.getReader().getPhone(),
                loan.getBorrowedAt(),
                loan.getDueDate(),
                loan.getReturnedAt(),
                loan.getStatus()
        );
    }
}