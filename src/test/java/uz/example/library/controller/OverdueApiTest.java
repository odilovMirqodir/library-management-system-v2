package uz.example.library.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uz.example.library.entity.Loan;
import uz.example.library.repository.LoanRepository;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OverdueApiTest {

    private static final AtomicLong COUNTER =
            new AtomicLong(System.currentTimeMillis());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void overdueLoan_shouldAppearInOverdueList()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook();

        String borrowBody = """
                {
                    "readerId": %d,
                    "bookId": %d
                }
                """.formatted(
                readerId,
                bookId
        );

        MvcResult borrowResult = mockMvc.perform(
                        post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(borrowBody)
                )
                .andExpect(status().isCreated())
                .andReturn();

        Number loanId = JsonPath.read(
                borrowResult
                        .getResponse()
                        .getContentAsString(),
                "$.id"
        );

        Loan loan = loanRepository
                .findById(loanId.longValue())
                .orElseThrow();

        loan.setDueDate(
                LocalDateTime.now().minusDays(5)
        );

        loanRepository.save(loan);

        mockMvc.perform(
                        get("/api/v1/loans/overdue")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                )
                .andExpect(
                        jsonPath("$.totalElements").value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].loanId")
                                .value(loanId.longValue())
                );
    }

    @Test
    void returnedLoan_shouldNotAppearInOverdueList()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook();

        String borrowBody = """
                {
                    "readerId": %d,
                    "bookId": %d
                }
                """.formatted(
                readerId,
                bookId
        );

        MvcResult borrowResult = mockMvc.perform(
                        post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(borrowBody)
                )
                .andExpect(status().isCreated())
                .andReturn();

        Number loanId = JsonPath.read(
                borrowResult
                        .getResponse()
                        .getContentAsString(),
                "$.id"
        );

        Loan loan = loanRepository
                .findById(loanId.longValue())
                .orElseThrow();

        loan.setDueDate(
                LocalDateTime.now().minusDays(5)
        );

        loanRepository.save(loan);

        mockMvc.perform(
                        post(
                                "/api/v1/loans/{id}/return",
                                loanId.longValue()
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/v1/loans/overdue")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalElements").value(0)
                );
    }

    private Long createReader()
            throws Exception {

        long value =
                COUNTER.incrementAndGet()
                        % 10_000_000L;

        String phone =
                String.format(
                        "+99890%07d",
                        value
                );

        String body = """
                {
                    "fullName": "Overdue Test Reader",
                    "phone": "%s",
                    "email": "overdue.test@gmail.com"
                }
                """.formatted(phone);

        MvcResult result = mockMvc.perform(
                        post("/api/v1/readers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }

    private Long createAuthor()
            throws Exception {

        String body = """
                {
                    "fullName": "Overdue Test Author",
                    "birthDate": "1990-01-01",
                    "country": "Uzbekistan",
                    "biography": "Test"
                }
                """;

        MvcResult result = mockMvc.perform(
                        post("/api/v1/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }

    private Long createCategory()
            throws Exception {

        String name =
                "Overdue Category "
                        + COUNTER.incrementAndGet();

        String body = """
                {
                    "name": "%s",
                    "description": "Overdue test"
                }
                """.formatted(name);

        MvcResult result = mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }

    private Long createBook()
            throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        long value =
                COUNTER.incrementAndGet()
                        % 1_000_000_000_000L;

        String isbn =
                String.format(
                        "9%012d",
                        value
                );

        String body = """
                {
                    "isbn": "%s",
                    "title": "Overdue Test Book",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2025,
                    "totalCopies": 2
                }
                """.formatted(
                isbn,
                authorId,
                categoryId
        );

        MvcResult result = mockMvc.perform(
                        post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }
}