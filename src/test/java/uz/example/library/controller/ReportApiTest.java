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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportApiTest {

    private static final AtomicLong COUNTER =
            new AtomicLong(System.currentTimeMillis());

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reportSummary_shouldReturn200AndAllFields()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/reports/summary")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitles").exists())
                .andExpect(jsonPath("$.totalCopies").exists())
                .andExpect(jsonPath("$.availableCopies").exists())
                .andExpect(jsonPath("$.borrowedBooks").exists())
                .andExpect(jsonPath("$.activeReaders").exists())
                .andExpect(jsonPath("$.overdueLoans").exists());
    }

    @Test
    void reportSummary_shouldChangeAfterCreatingBookAndReader()
            throws Exception {

        MvcResult beforeResult = mockMvc.perform(
                        get("/api/v1/reports/summary")
                )
                .andExpect(status().isOk())
                .andReturn();

        String beforeJson =
                beforeResult.getResponse().getContentAsString();

        Number oldBookTitles =
                JsonPath.read(beforeJson, "$.bookTitles");

        Number oldTotalCopies =
                JsonPath.read(beforeJson, "$.totalCopies");

        Number oldAvailableCopies =
                JsonPath.read(beforeJson, "$.availableCopies");

        Number oldActiveReaders =
                JsonPath.read(beforeJson, "$.activeReaders");

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        createBook(
                authorId,
                categoryId,
                3
        );

        createReader();

        MvcResult afterResult = mockMvc.perform(
                        get("/api/v1/reports/summary")
                )
                .andExpect(status().isOk())
                .andReturn();

        String afterJson =
                afterResult.getResponse().getContentAsString();

        Number newBookTitles =
                JsonPath.read(afterJson, "$.bookTitles");

        Number newTotalCopies =
                JsonPath.read(afterJson, "$.totalCopies");

        Number newAvailableCopies =
                JsonPath.read(afterJson, "$.availableCopies");

        Number newActiveReaders =
                JsonPath.read(afterJson, "$.activeReaders");

        org.junit.jupiter.api.Assertions.assertEquals(
                oldBookTitles.longValue() + 1,
                newBookTitles.longValue()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                oldTotalCopies.longValue() + 3,
                newTotalCopies.longValue()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                oldAvailableCopies.longValue() + 3,
                newAvailableCopies.longValue()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                oldActiveReaders.longValue() + 1,
                newActiveReaders.longValue()
        );
    }

    private Long createAuthor()
            throws Exception {

        String body = """
                {
                    "fullName": "Report Test Author",
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
                "Report Category "
                        + COUNTER.incrementAndGet();

        String body = """
                {
                    "name": "%s",
                    "description": "Report test category"
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

    private void createBook(
            Long authorId,
            Long categoryId,
            int copies
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
                    "title": "Report Test Book",
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

        mockMvc.perform(
                        post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    private void createReader()
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
                    "fullName": "Report Test Reader",
                    "phone": "%s",
                    "email": "report.test@gmail.com"
                }
                """.formatted(phone);

        mockMvc.perform(
                        post("/api/v1/readers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());
    }
}