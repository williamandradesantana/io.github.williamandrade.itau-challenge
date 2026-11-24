
package io.github.williamandradesantana.itau_challenge.interfaces.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.PostgresIntegrationTest;
import io.github.williamandradesantana.itau_challenge.interfaces.transaction.dtos.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TransactionControllerTest extends PostgresIntegrationTest {

    private static final String BASE_URL = "/api/transactions/v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.create(
                new BigDecimal("100.00"),
                OffsetDateTime.now(Clock.systemUTC()).minusSeconds(10)
        );
    }

    @Test
    @WithMockUser(roles = "COMMON")
    @DisplayName("Test: creating a transaction with valid data should return 201")
    void test_CreatingTransactionWithValidData_ShouldReturn201() throws Exception {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                OffsetDateTime.now(Clock.systemUTC()).minusSeconds(10)
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test: creating a transaction with null amount should return 400")
    void test_CreatingTransactionWithNullAmount_ShouldReturn400() throws Exception {
        TransactionRequest request = new TransactionRequest(
                null,
                OffsetDateTime.now(Clock.systemUTC()).minusSeconds(10)
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test: creating a transaction with null datetime should return 400")
    void test_CreatingTransactionWithNullDateTime_ShouldReturn400() throws Exception {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "COMMON")
    @DisplayName("Test: retrieving transaction statistics should return 200")
    void test_RetrievingTransactionStatistics_ShouldReturn200() throws Exception {
        transactionRepository.save(transaction);

        mockMvc.perform(get(BASE_URL + "/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.sum").value(100.00))
                .andExpect(jsonPath("$.min").value(100.00))
                .andExpect(jsonPath("$.max").value(100.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test: deleting an existing transaction should return 204")
    void test_DeletingExistingTransaction_ShouldReturn204() throws Exception {
        transactionRepository.save(transaction);

        mockMvc.perform(delete(BASE_URL + "/{id}", transaction.getId()))
                .andExpect(status().isNoContent());

        assertTrue(
                transactionRepository.findById(transaction.getId()).isEmpty()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test: deleting a non-existent transaction should return 404")
    void test_DeletingNonExistentTransaction_ShouldReturn404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Test: accessing statistics without permission should return 403")
    void test_AccessingStatisticsWithoutPermission_ShouldReturn403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/stats"))
                .andExpect(status().isForbidden());
    }
}
