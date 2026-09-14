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

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeleteRuleApiTest {

    private static final AtomicLong COUNTER =
            new AtomicLong(System.currentTimeMillis());

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deleteAuthorWithBook_shouldReturn409()
            throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        createBook(
                authorId,
                categoryId
        );

        mockMvc.perform(
                        delete("/api/v1/authors/{id}", authorId)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void deleteCategoryWithBook_shouldReturn409()
            throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        createBook(
                authorId,
                categoryId
        );

        mockMvc.perform(
                        delete("/api/v1/categories/{id}", categoryId)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void deleteReaderWithActiveLoan_shouldReturn409()
            throws Exception {

        Long readerId = createReader();

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        Long bookId = createBook(
                authorId,
                categoryId
        );

        borrow(
                readerId,
                bookId
        );

        mockMvc.perform(
                        delete("/api/v1/readers/{id}", readerId)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void deleteBookWithLoanHistory_shouldMakeBookInactive()
            throws Exception {

        Long readerId = createReader();

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        Long bookId = createBook(
                authorId,
                categoryId
        );

        Long loanId = borrow(
                readerId,
                bookId
        );

        mockMvc.perform(
                        post(
                                "/api/v1/loans/{id}/return",
                                loanId
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/api/v1/books/{id}", bookId)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/books/{id}", bookId)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("INACTIVE")
                );
    }

    private Long createAuthor()
            throws Exception {

        String body = """
                {
                    "fullName": "Delete Rule Author",
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
                "Delete Category "
                        + COUNTER.incrementAndGet();

        String body = """
                {
                    "name": "%s",
                    "description": "Test"
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
                    "fullName": "Delete Rule Reader",
                    "phone": "%s",
                    "email": "delete.rule@gmail.com"
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

    private Long createBook(
            Long authorId,
            Long categoryId
    ) throws Exception {

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
                    "title": "Delete Rule Book",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2025,
                    "totalCopies": 3
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

    private Long borrow(
            Long readerId,
            Long bookId
    ) throws Exception {

        String body = """
                {
                    "readerId": %d,
                    "bookId": %d
                }
                """.formatted(
                readerId,
                bookId
        );

        MvcResult result = mockMvc.perform(
                        post("/api/v1/loans")
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