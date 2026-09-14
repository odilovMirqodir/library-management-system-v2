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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createBook_shouldReturn201() throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String isbn = uniqueIsbn();

        String body = """
                {
                    "isbn": "%s",
                    "title": "Java Test Book",
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
                        post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(
                        jsonPath("$.isbn")
                                .value(isbn)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Java Test Book")
                )
                .andExpect(
                        jsonPath("$.totalCopies")
                                .value(5)
                )
                .andExpect(
                        jsonPath("$.availableCopies")
                                .value(5)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                );
    }

    @Test
    void createBook_invalidIsbn_shouldReturn400() throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String body = """
                {
                    "isbn": "123",
                    "title": "Java Test Book",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2025,
                    "totalCopies": 5
                }
                """.formatted(
                authorId,
                categoryId
        );

        mockMvc.perform(
                        post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );
    }

    @Test
    void createDuplicateIsbn_shouldReturn409() throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String isbn = uniqueIsbn();

        createBook(
                isbn,
                "Birinchi kitob",
                authorId,
                categoryId,
                2020,
                3
        );

        String secondBody = """
                {
                    "isbn": "%s",
                    "title": "Ikkinchi kitob",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2021,
                    "totalCopies": 2
                }
                """.formatted(
                isbn,
                authorId,
                categoryId
        );

        mockMvc.perform(
                        post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondBody)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("CONFLICT")
                );
    }

    @Test
    void createBook_unknownAuthor_shouldReturn404() throws Exception {

        Long categoryId = createCategory();

        String body = """
                {
                    "isbn": "%s",
                    "title": "Unknown Author Book",
                    "authorId": 999999999,
                    "categoryId": %d,
                    "publishedYear": 2020,
                    "totalCopies": 2
                }
                """.formatted(
                uniqueIsbn(),
                categoryId
        );

        mockMvc.perform(
                        post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Muallif topilmadi")
                );
    }

    @Test
    void getUnknownBook_shouldReturn404() throws Exception {

        mockMvc.perform(
                        get("/api/v1/books/999999999")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Kitob topilmadi")
                );
    }

    @Test
    void combinedFilter_shouldReturnMatchingBook() throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String isbn = uniqueIsbn();

        createBook(
                isbn,
                "Spring Boot Test",
                authorId,
                categoryId,
                2024,
                4
        );

        mockMvc.perform(
                        get("/api/v1/books")
                                .param(
                                        "authorId",
                                        authorId.toString()
                                )
                                .param(
                                        "categoryId",
                                        categoryId.toString()
                                )
                                .param(
                                        "available",
                                        "true"
                                )
                                .param(
                                        "title",
                                        "Spring"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                )
                .andExpect(
                        jsonPath("$.totalElements").value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].isbn")
                                .value(isbn)
                );
    }

    @Test
    void combinedFilter_wrongCategory_shouldReturnEmpty() throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        createBook(
                uniqueIsbn(),
                "Filter Test Book",
                authorId,
                categoryId,
                2020,
                2
        );

        mockMvc.perform(
                        get("/api/v1/books")
                                .param(
                                        "authorId",
                                        authorId.toString()
                                )
                                .param(
                                        "categoryId",
                                        "999999999"
                                )
                                .param(
                                        "available",
                                        "true"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isEmpty()
                )
                .andExpect(
                        jsonPath("$.totalElements").value(0)
                );
    }

    @Test
    void sortByYear_shouldReturnNewestFirst() throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String oldIsbn = uniqueIsbn();
        String newIsbn = uniqueIsbn();

        createBook(
                oldIsbn,
                "Eski kitob",
                authorId,
                categoryId,
                1990,
                2
        );

        createBook(
                newIsbn,
                "Yangi kitob",
                authorId,
                categoryId,
                2025,
                2
        );

        mockMvc.perform(
                        get("/api/v1/books")
                                .param(
                                        "authorId",
                                        authorId.toString()
                                )
                                .param(
                                        "categoryId",
                                        categoryId.toString()
                                )
                                .param(
                                        "sort",
                                        "year"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content[0].publishedYear")
                                .value(2025)
                )
                .andExpect(
                        jsonPath("$.content[1].publishedYear")
                                .value(1990)
                );
    }

    @Test
    void pagination_shouldReturnCorrectPageData() throws Exception {

        mockMvc.perform(
                        get("/api/v1/books")
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(
                        jsonPath("$.totalElements").exists()
                )
                .andExpect(
                        jsonPath("$.totalPages").exists()
                );
    }

    @Test
    void invalidPage_shouldReturn400() throws Exception {

        mockMvc.perform(
                        get("/api/v1/books")
                                .param("page", "-1")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_ERROR")
                );
    }

    private Long createAuthor() throws Exception {

        String body = """
                {
                    "fullName": "Book Test Author",
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

        String categoryName =
                "Book Test Category "
                        + System.nanoTime();

        String body = """
                {
                    "name": "%s",
                    "description": "Test category"
                }
                """.formatted(categoryName);

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
            String isbn,
            String title,
            Long authorId,
            Long categoryId,
            int year,
            int copies
    ) throws Exception {

        String body = """
                {
                    "isbn": "%s",
                    "title": "%s",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": %d,
                    "totalCopies": %d
                }
                """.formatted(
                isbn,
                title,
                authorId,
                categoryId,
                year,
                copies
        );

        mockMvc.perform(
                        post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    private String uniqueIsbn() {

        long value =
                Math.abs(
                        System.nanoTime()
                                % 1_000_000_000_000L
                );

        return String.format(
                "9%012d",
                value
        );
    }
}