package uz.example.library.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthorApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAuthor_shouldReturn201() throws Exception {

        String requestBody = """
                {
                    "fullName": "   Test Author   ",
                    "birthDate": "1990-01-01",
                    "country": "   Uzbekistan   ",
                    "biography": "   Test biography   "
                }
                """;

        mockMvc.perform(
                        post("/api/v1/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Test Author"))
                .andExpect(jsonPath("$.country").value("Uzbekistan"))
                .andExpect(jsonPath("$.biography").value("Test biography"));
    }

    @Test
    void createAuthor_blankFullName_shouldReturn400() throws Exception {

        String requestBody = """
                {
                    "fullName": " ",
                    "birthDate": "1990-01-01",
                    "country": "Uzbekistan",
                    "biography": "Test"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("FIELD_VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.fullName").exists()
                );
    }

    @Test
    void createAuthor_futureBirthDate_shouldReturn400() throws Exception {

        String requestBody = """
                {
                    "fullName": "Test Author",
                    "birthDate": "2099-01-01",
                    "country": "Uzbekistan",
                    "biography": "Test"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("FIELD_VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.birthDate").exists()
                );
    }

    @Test
    void getUnknownAuthor_shouldReturn404() throws Exception {

        mockMvc.perform(
                        get("/api/v1/authors/999999999")
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
    void getAuthors_shouldReturnPagination() throws Exception {

        mockMvc.perform(
                        get("/api/v1/authors")
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }
}