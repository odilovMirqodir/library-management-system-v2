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
import uz.example.library.entity.Book;
import uz.example.library.entity.Reader;
import uz.example.library.enums.ReaderStatus;
import uz.example.library.repository.BookRepository;
import uz.example.library.repository.ReaderRepository;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanApiTest {

    private static final AtomicLong COUNTER =
            new AtomicLong(
                    System.currentTimeMillis() % 1_000_000_000L
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void borrowBook_shouldReturn201AndDecreaseAvailableCopies()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook(3);

        MvcResult result = borrow(
                readerId,
                bookId,
                201
        );

        String response =
                result.getResponse().getContentAsString();

        Number loanId =
                JsonPath.read(response, "$.id");

        mockMvc.perform(
                        get("/api/v1/books/{id}", bookId)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.availableCopies").value(2)
                );

        mockMvc.perform(
                        get("/api/v1/loans")
                                .param(
                                        "readerId",
                                        readerId.toString()
                                )
                                .param(
                                        "bookId",
                                        bookId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(loanId.longValue())
                )
                .andExpect(
                        jsonPath("$.content[0].status")
                                .value("BORROWED")
                );
    }

    @Test
    void borrowUnknownReader_shouldReturn404()
            throws Exception {

        Long bookId = createBook(2);

        String body = """
                {
                    "readerId": 999999999,
                    "bookId": %d
                }
                """.formatted(bookId);

        mockMvc.perform(
                        post("/api/v1/loans")
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
                                .value("Kitobxon topilmadi")
                );
    }

    @Test
    void borrowUnknownBook_shouldReturn404()
            throws Exception {

        Long readerId = createReader();

        String body = """
                {
                    "readerId": %d,
                    "bookId": 999999999
                }
                """.formatted(readerId);

        mockMvc.perform(
                        post("/api/v1/loans")
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
                                .value("Kitob topilmadi")
                );
    }

    @Test
    void borrowSameBookTwice_shouldReturn409()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook(5);

        borrow(readerId, bookId, 201);
        borrow(readerId, bookId, 409);
    }

    @Test
    void blockedReader_shouldNotBorrowBook()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook(2);

        Reader reader =
                readerRepository.findById(readerId)
                        .orElseThrow();

        reader.setStatus(ReaderStatus.BLOCKED);
        readerRepository.save(reader);

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
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void unavailableBook_shouldReturn409()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook(1);

        Book book =
                bookRepository.findById(bookId)
                        .orElseThrow();

        book.setAvailableCopies(0);
        bookRepository.save(book);

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
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void returnBook_shouldChangeStatusAndRestoreCopy()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook(2);

        MvcResult borrowResult =
                borrow(
                        readerId,
                        bookId,
                        201
                );

        Number loanId =
                JsonPath.read(
                        borrowResult
                                .getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/loans/{id}/return",
                                loanId.longValue()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("RETURNED")
                )
                .andExpect(
                        jsonPath("$.returnedAt").exists()
                );

        mockMvc.perform(
                        get("/api/v1/books/{id}", bookId)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.availableCopies")
                                .value(2)
                );
    }

    @Test
    void returnBookTwice_shouldReturn409()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook(2);

        MvcResult borrowResult =
                borrow(
                        readerId,
                        bookId,
                        201
                );

        Number loanId =
                JsonPath.read(
                        borrowResult
                                .getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/loans/{id}/return",
                                loanId.longValue()
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(
                                "/api/v1/loans/{id}/return",
                                loanId.longValue()
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void combinedLoanFilter_shouldWork()
            throws Exception {

        Long readerId = createReader();
        Long bookId = createBook(2);

        borrow(readerId, bookId, 201);

        mockMvc.perform(
                        get("/api/v1/loans")
                                .param(
                                        "readerId",
                                        readerId.toString()
                                )
                                .param(
                                        "bookId",
                                        bookId.toString()
                                )
                                .param(
                                        "status",
                                        "BORROWED"
                                )
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].readerId")
                                .value(readerId)
                )
                .andExpect(
                        jsonPath("$.content[0].bookId")
                                .value(bookId)
                )
                .andExpect(
                        jsonPath("$.content[0].status")
                                .value("BORROWED")
                );
    }

    @Test
    void sixthActiveLoan_shouldReturn409()
            throws Exception {

        Long readerId = createReader();

        for (int i = 0; i < 5; i++) {

            Long bookId = createBook(2);

            borrow(
                    readerId,
                    bookId,
                    201
            );
        }

        Long sixthBookId = createBook(2);

        String body = """
                {
                    "readerId": %d,
                    "bookId": %d
                }
                """.formatted(
                readerId,
                sixthBookId
        );

        mockMvc.perform(
                        post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("BUSINESS_RULE_CONFLICT")
                );
    }

    @Test
    void loanPagination_shouldWork()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/loans")
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

    private MvcResult borrow(
            Long readerId,
            Long bookId,
            int expectedStatus
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

        return mockMvc.perform(
                        post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(
                        status().is(expectedStatus)
                )
                .andReturn();
    }

    private Long createReader()
            throws Exception {

        String phone = uniquePhone();

        String body = """
                {
                    "fullName": "Loan Test Reader",
                    "phone": "%s",
                    "email": "loan.test@gmail.com"
                }
                """.formatted(phone);

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/readers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body)
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        Number id =
                JsonPath.read(
                        result.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        return id.longValue();
    }

    private Long createAuthor()
            throws Exception {

        String body = """
                {
                    "fullName": "Loan Test Author",
                    "birthDate": "1990-01-01",
                    "country": "Uzbekistan",
                    "biography": "Test"
                }
                """;

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/authors")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body)
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        Number id =
                JsonPath.read(
                        result.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        return id.longValue();
    }

    private Long createCategory()
            throws Exception {

        String body = """
                {
                    "name": "%s",
                    "description": "Loan test category"
                }
                """.formatted(
                "Loan Category "
                        + COUNTER.incrementAndGet()
        );

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/categories")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body)
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        Number id =
                JsonPath.read(
                        result.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        return id.longValue();
    }

    private Long createBook(
            int copies
    ) throws Exception {

        Long authorId = createAuthor();
        Long categoryId = createCategory();

        String body = """
                {
                    "isbn": "%s",
                    "title": "Loan Test Book %d",
                    "authorId": %d,
                    "categoryId": %d,
                    "publishedYear": 2025,
                    "totalCopies": %d
                }
                """.formatted(
                uniqueIsbn(),
                COUNTER.incrementAndGet(),
                authorId,
                categoryId,
                copies
        );

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/books")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body)
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        Number id =
                JsonPath.read(
                        result.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        return id.longValue();
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