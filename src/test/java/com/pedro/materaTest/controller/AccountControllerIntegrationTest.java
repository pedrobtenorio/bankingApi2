package com.pedro.materaTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedro.materaTest.domain.enums.EntryType;
import com.pedro.materaTest.dto.BatchLedgerEntryRequest;
import com.pedro.materaTest.dto.CreateAccountRequest;
import com.pedro.materaTest.dto.LedgerEntryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AccountControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createAccount_thenApplyEntries_andGetBalance() throws Exception {
        CreateAccountRequest createRequest = new CreateAccountRequest(new BigDecimal("100.00"));
        String createResponse = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.balance").value(100.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID accountId = UUID.fromString(objectMapper.readTree(createResponse).get("accountId").asText());

        BatchLedgerEntryRequest batchRequest = new BatchLedgerEntryRequest(List.of(
                new LedgerEntryRequest(EntryType.DEBIT, new BigDecimal("40.00"), "debit"),
                new LedgerEntryRequest(EntryType.CREDIT, new BigDecimal("10.00"), "credit")));

        mockMvc.perform(post("/accounts/{accountId}/entries", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(70.00))
                .andExpect(jsonPath("$.entries.length()").value(2));

        mockMvc.perform(get("/accounts/{accountId}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(70.00));
    }

    @Test
    void applyEntries_returns422WhenInsufficientFunds() throws Exception {
        CreateAccountRequest createRequest = new CreateAccountRequest(new BigDecimal("10.00"));
        String createResponse = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID accountId = UUID.fromString(objectMapper.readTree(createResponse).get("accountId").asText());

        BatchLedgerEntryRequest batchRequest = new BatchLedgerEntryRequest(List.of(
                new LedgerEntryRequest(EntryType.DEBIT, new BigDecimal("50.00"), "debit")));

        mockMvc.perform(post("/accounts/{accountId}/entries", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
