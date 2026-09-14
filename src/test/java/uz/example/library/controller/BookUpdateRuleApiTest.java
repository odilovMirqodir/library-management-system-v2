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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookUpdateRuleApiTest {

    private static final AtomicLong COUNTER =
            new AtomicLong(System.currentTimeMillis());

    @Autowired
    private MockMvc mockMvc;

    @Test
    void updateTotalCopiesBelowActiveLoans_shouldReturn409()
            throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String isbn = uniqueIsbn();

        Long bookId = createBook(
                isbn,
                authorId,
                categoryId,
                3
        );

        Long reader1 = createReader();
        Long reader2 = createReader();

        borrow(reader1, bookId);
        borrow(reader2, bookId);

        String updateBody = """
                {
                    "isbn": "%s",
                    "title": "Updated Test Book",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2025,
                    "totalCopies": 1
                }
                """.formatted(
                isbn,
                authorId,
                categoryId
        );

        mockMvc.perform(
                        put("/api/v1/books/{id}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void updateTotalCopies_shouldRecalculateAvailableCopies()
            throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String isbn = uniqueIsbn();

        Long bookId = createBook(
                isbn,
                authorId,
                categoryId,
                3
        );

        Long reader1 = createReader();
        Long reader2 = createReader();

        borrow(reader1, bookId);
        borrow(reader2, bookId);

        String updateBody = """
                {
                    "isbn": "%s",
                    "title": "Updated Test Book",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2025,
                    "totalCopies": 5
                }
                """.formatted(
                isbn,
                authorId,
                categoryId
        );

        mockMvc.perform(
                        put("/api/v1/books/{id}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalCopies")
                                .value(5)
                )
                .andExpect(
                        jsonPath("$.availableCopies")
                                .value(3)
                );
    }

    private Long createAuthor() throws Exception {

        String body = """
                {
                    "fullName": "Book Update Test Author",
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

    private Long createCategory() throws Exception {

        String name =
                "Book Update Category "
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

    private Long createReader() throws Exception {

        String phone = uniquePhone();

        String body = """
                {
                    "fullName": "Book Update Test Reader",
                    "phone": "%s",
                    "email": "book.update@gmail.com"
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
            String isbn,
            Long authorId,
            Long categoryId,
            int copies
    ) throws Exception {

        String body = """
                {
                    "isbn": "%s",
                    "title": "Book Update Test",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2025,
                    "totalCopies": %d
                }
                """.formatted(
                isbn,
                authorId,
                categoryId,
                copies
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

    private void borrow(
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

        mockMvc.perform(
                        post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    private String uniquePhone() {

        long value =
                COUNTER.incrementAndGet()
                        % 10_000_000L;

        return String.format(
                "+99890%07d",
                value
        );
    }

    private String uniqueIsbn() {

        long value =
                COUNTER.incrementAndGet()
                        % 1_000_000_000_000L;

        return String.format(
                "9%012d",
                value
        );
    }
}