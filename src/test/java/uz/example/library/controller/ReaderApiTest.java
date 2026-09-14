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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReaderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createReader_shouldReturn201()
            throws Exception {

        String phone = uniquePhone();

        String body = """
                {
                    "fullName": "   Test Reader   ",
                    "phone": "%s",
                    "email": "test.reader@gmail.com"
                }
                """.formatted(phone);

        mockMvc.perform(
                        post("/api/v1/readers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(
                        jsonPath("$.fullName")
                                .value("Test Reader")
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value(phone)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("test.reader@gmail.com")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                )
                .andExpect(
                        jsonPath("$.registeredAt").exists()
                );
    }

    @Test
    void createReader_blankName_shouldReturn400()
            throws Exception {

        String body = """
                {
                    "fullName": " ",
                    "phone": "%s",
                    "email": "test@gmail.com"
                }
                """.formatted(uniquePhone());

        mockMvc.perform(
                        post("/api/v1/readers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
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
    void createReader_invalidEmail_shouldReturn400()
            throws Exception {

        String body = """
                {
                    "fullName": "Test Reader",
                    "phone": "%s",
                    "email": "email-noto-gri"
                }
                """.formatted(uniquePhone());

        mockMvc.perform(
                        post("/api/v1/readers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("FIELD_VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.email").exists()
                );
    }

    @Test
    void createDuplicatePhone_shouldReturn409()
            throws Exception {

        String phone = uniquePhone();

        createReader(
                "Birinchi Reader",
                phone,
                "first@gmail.com"
        );

        String secondBody = """
                {
                    "fullName": "Ikkinchi Reader",
                    "phone": "%s",
                    "email": "second@gmail.com"
                }
                """.formatted(phone);

        mockMvc.perform(
                        post("/api/v1/readers")
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
    void getUnknownReader_shouldReturn404()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/readers/999999999")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Kitobxon topilmadi")
                );
    }

    @Test
    void searchReader_shouldReturnMatchingReader()
            throws Exception {

        String phone = uniquePhone();

        createReader(
                "Mirqodir Test Reader",
                phone,
                "mirqodir.test@gmail.com"
        );

        mockMvc.perform(
                        get("/api/v1/readers")
                                .param("search", "Mirqodir")
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
                        jsonPath("$.content[0].fullName")
                                .value("Mirqodir Test Reader")
                );
    }

    @Test
    void filterReaderByStatus_shouldReturnActiveReader()
            throws Exception {

        createReader(
                "Active Test Reader",
                uniquePhone(),
                "active.test@gmail.com"
        );

        mockMvc.perform(
                        get("/api/v1/readers")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                );
    }

    @Test
    void updateReader_shouldReturnUpdatedData()
            throws Exception {

        Long readerId = createReader(
                "Old Reader",
                uniquePhone(),
                "old@gmail.com"
        );

        String newPhone = uniquePhone();

        String body = """
                {
                    "fullName": "   Updated Reader   ",
                    "phone": "%s",
                    "email": "updated@gmail.com"
                }
                """.formatted(newPhone);

        mockMvc.perform(
                        put("/api/v1/readers/{id}", readerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.fullName")
                                .value("Updated Reader")
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value(newPhone)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("updated@gmail.com")
                );
    }

    @Test
    void deleteReaderWithoutHistory_shouldReturn204()
            throws Exception {

        Long readerId = createReader(
                "Delete Test Reader",
                uniquePhone(),
                "delete.test@gmail.com"
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/readers/{id}",
                                readerId
                        )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get(
                                "/api/v1/readers/{id}",
                                readerId
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void pagination_shouldReturnCorrectData()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/readers")
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                )
                .andExpect(
                        jsonPath("$.page").value(0)
                )
                .andExpect(
                        jsonPath("$.size").value(1)
                )
                .andExpect(
                        jsonPath("$.totalElements").exists()
                )
                .andExpect(
                        jsonPath("$.totalPages").exists()
                );
    }

    private Long createReader(
            String fullName,
            String phone,
            String email
    ) throws Exception {

        String body = """
                {
                    "fullName": "%s",
                    "phone": "%s",
                    "email": "%s"
                }
                """.formatted(
                fullName,
                phone,
                email
        );

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

    private String uniquePhone() {

        long number =
                Math.abs(
                        System.nanoTime()
                                % 10_000_000L
                );

        return String.format(
                "+99890%07d",
                number
        );
    }
}