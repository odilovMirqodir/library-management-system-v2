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
class ErrorHandlingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void invalidEnum_shouldReturn400()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/books")
                                .param(
                                        "status",
                                        "NOT_A_REAL_STATUS"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("INVALID_PARAMETER")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/v1/books")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.status")
                                .exists()
                );
    }

    @Test
    void invalidPageType_shouldReturn400()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/books")
                                .param(
                                        "page",
                                        "abc"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("INVALID_PARAMETER")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.page")
                                .exists()
                );
    }

    @Test
    void malformedJson_shouldReturn400()
            throws Exception {

        String invalidJson = """
                {
                    "fullName": "Test Author",
                    "birthDate":
                }
                """;

        mockMvc.perform(
                        post("/api/v1/authors")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("INVALID_REQUEST_BODY")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "JSON ma'lumoti noto'g'ri formatda"
                                )
                )
                .andExpect(
                        jsonPath("$.fieldErrors")
                                .isMap()
                );
    }

    @Test
    void unknownEndpoint_shouldReturn404()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/unknown-endpoint")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Endpoint topilmadi")
                )
                .andExpect(
                        jsonPath("$.fieldErrors")
                                .isMap()
                );
    }
}