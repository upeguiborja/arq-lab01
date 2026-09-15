package com.udea.banco.banco2025.controller;

import com.udea.banco.banco2025.DTO.TransactionDTO;
import com.udea.banco.banco2025.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    void getAllTransactions_ShouldReturnListAnd200() throws Exception {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(1L);
        dto.setSenderAccountNumber("123456789");
        dto.setReceiverAccountNumber("987654321");
        dto.setAmount(100.0);
        dto.setTimestamp(LocalDateTime.now());

        when(transactionService.getAllTransactions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].senderAccountNumber").value("123456789"))
                .andExpect(jsonPath("$[0].receiverAccountNumber").value("987654321"))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[0].timestamp").isNotEmpty());
    }

    @Test
    void getTransactionById_WhenFound_ShouldReturnDtoAnd200() throws Exception {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(1L);
        dto.setSenderAccountNumber("123456789");
        dto.setReceiverAccountNumber("987654321");
        dto.setAmount(100.0);
        dto.setTimestamp(LocalDateTime.now());

        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderAccountNumber").value("123456789"))
                .andExpect(jsonPath("$.receiverAccountNumber").value("987654321"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void getTransactionById_WhenNotFound_ShouldReturn404() throws Exception {
        when(transactionService.getTransactionById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_ShouldReturnDtoWithIdAndTimestampAnd200() throws Exception {
        TransactionDTO responseDto = new TransactionDTO();
        responseDto.setId(1L);
        responseDto.setSenderAccountNumber("123456789");
        responseDto.setReceiverAccountNumber("987654321");
        responseDto.setAmount(100.0);
        responseDto.setTimestamp(LocalDateTime.now());

        when(transactionService.createTransaction(any(TransactionDTO.class))).thenReturn(responseDto);

        String jsonRequest = "{\"senderAccountNumber\":\"123456789\",\"receiverAccountNumber\":\"987654321\",\"amount\":100.00}";

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderAccountNumber").value("123456789"))
                .andExpect(jsonPath("$.receiverAccountNumber").value("987654321"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void updateTransaction_WhenFound_ShouldReturnUpdatedDtoAnd200() throws Exception {
        TransactionDTO updatedDto = new TransactionDTO();
        updatedDto.setId(1L);
        updatedDto.setSenderAccountNumber("123456789");
        updatedDto.setReceiverAccountNumber("987654321");
        updatedDto.setAmount(100.0);
        updatedDto.setTimestamp(LocalDateTime.now());

        when(transactionService.updateTransaction(eq(1L), any(TransactionDTO.class))).thenReturn(Optional.of(updatedDto));

        String jsonRequest = "{\"senderAccountNumber\":\"123456789\",\"receiverAccountNumber\":\"987654321\",\"amount\":100.00}";

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderAccountNumber").value("123456789"))
                .andExpect(jsonPath("$.receiverAccountNumber").value("987654321"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void updateTransaction_WhenNotFound_ShouldReturn404() throws Exception {
        when(transactionService.updateTransaction(eq(99L), any(TransactionDTO.class))).thenReturn(Optional.empty());

        String jsonRequest = "{\"senderAccountNumber\":\"123456789\",\"receiverAccountNumber\":\"987654321\",\"amount\":100.00}";

        mockMvc.perform(put("/api/transactions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransaction_WhenFound_ShouldReturn204() throws Exception {
        when(transactionService.deleteTransaction(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTransaction_WhenNotFound_ShouldReturn404() throws Exception {
        when(transactionService.deleteTransaction(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }
}
