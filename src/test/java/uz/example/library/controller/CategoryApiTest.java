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
class CategoryApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createCategory_shouldReturn201() throws Exception {

        String requestBody = """
                {
                    "name": "   Test Category   ",
                    "description": "   Test description   "
                }
                """;

        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(
                        jsonPath("$.description")
                                .value("Test description")
                );
    }

    @Test
    void createCategory_blankName_shouldReturn400() throws Exception {

        String requestBody = """
                {
                    "name": " ",
                    "description": "Test"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("FIELD_VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.name").exists()
                );
    }

    @Test
    void createDuplicateCategory_shouldReturn409() throws Exception {

        String firstRequest = """
                {
                    "name": "Test Unique Category",
                    "description": "Birinchi kategoriya"
                }
                """;

        String secondRequest = """
                {
                    "name": "test unique category",
                    "description": "Ikkinchi kategoriya"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("CONFLICT")
                );
    }

    @Test
    void getUnknownCategory_shouldReturn404() throws Exception {

        mockMvc.perform(
                        get("/api/v1/categories/999999999")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Kategoriya topilmadi")
                );
    }

    @Test
    void getCategories_shouldReturnPagination() throws Exception {

        mockMvc.perform(
                        get("/api/v1/categories")
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }
}