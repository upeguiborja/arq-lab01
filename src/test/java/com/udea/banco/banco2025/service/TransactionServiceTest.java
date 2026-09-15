package com.udea.banco.banco2025.service;

import com.udea.banco.banco2025.DTO.TransactionDTO;
import com.udea.banco.banco2025.entity.Customer;
import com.udea.banco.banco2025.entity.Transaction;
import com.udea.banco.banco2025.mapper.TransactionMapper;
import com.udea.banco.banco2025.repository.CustomerRepository;
import com.udea.banco.banco2025.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Customer sender;
    private Customer receiver;
    private TransactionDTO transactionDTO;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        sender = new Customer();
        sender.setAccountNumber("123456789");
        sender.setBalance(500.0);

        receiver = new Customer();
        receiver.setAccountNumber("987654321");
        receiver.setBalance(200.0);

        transactionDTO = new TransactionDTO();
        transactionDTO.setSenderAccountNumber("123456789");
        transactionDTO.setReceiverAccountNumber("987654321");
        transactionDTO.setAmount(100.0);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSenderAccountNumber("123456789");
        transaction.setReceiverAccountNumber("987654321");
        transaction.setAmount(100.0);
        transaction.setTimestamp(LocalDateTime.now());
    }

    @Test
    void getAllTransactions_ShouldReturnList() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));
        when(transactionMapper.toDTO(transaction)).thenReturn(transactionDTO);

        List<TransactionDTO> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactionById_WhenExists_ShouldReturnOptionalWithDto() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDTO(transaction)).thenReturn(transactionDTO);

        Optional<TransactionDTO> result = transactionService.getTransactionById(1L);

        assertTrue(result.isPresent());
        assertEquals("123456789", result.get().getSenderAccountNumber());
    }

    @Test
    void getTransactionById_WhenNotExists_ShouldReturnEmptyOptional() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TransactionDTO> result = transactionService.getTransactionById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void transferMoney_ShouldTransferAndSetTimestamp() {
        when(customerRepository.findByAccountNumber("123456789")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("987654321")).thenReturn(Optional.of(receiver));

        Transaction entityWithoutTimestamp = new Transaction();
        entityWithoutTimestamp.setSenderAccountNumber("123456789");
        entityWithoutTimestamp.setReceiverAccountNumber("987654321");
        entityWithoutTimestamp.setAmount(100.0);

        when(transactionMapper.toEntity(transactionDTO)).thenReturn(entityWithoutTimestamp);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        TransactionDTO responseDTO = new TransactionDTO();
        responseDTO.setId(1L);
        responseDTO.setSenderAccountNumber("123456789");
        responseDTO.setReceiverAccountNumber("987654321");
        responseDTO.setAmount(100.0);
        responseDTO.setTimestamp(LocalDateTime.now());
        when(transactionMapper.toDTO(any(Transaction.class))).thenReturn(responseDTO);

        TransactionDTO result = transactionService.transferMoney(transactionDTO);

        assertNotNull(result);
        assertEquals(400.0, sender.getBalance());
        assertEquals(300.0, receiver.getBalance());
        assertNotNull(entityWithoutTimestamp.getTimestamp());
        verify(customerRepository).save(sender);
        verify(customerRepository).save(receiver);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_WhenFound_ShouldUpdateFieldsAndPreserveTimestamp() {
        LocalDateTime originalTimestamp = LocalDateTime.of(2026, 1, 1, 12, 0);
        transaction.setTimestamp(originalTimestamp);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDTO updatedResponseDTO = new TransactionDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setSenderAccountNumber("111111111");
        updatedResponseDTO.setReceiverAccountNumber("222222222");
        updatedResponseDTO.setAmount(200.0);
        updatedResponseDTO.setTimestamp(originalTimestamp);

        when(transactionMapper.toDTO(any(Transaction.class))).thenReturn(updatedResponseDTO);

        TransactionDTO updateRequest = new TransactionDTO();
        updateRequest.setSenderAccountNumber("111111111");
        updateRequest.setReceiverAccountNumber("222222222");
        updateRequest.setAmount(200.0);

        Optional<TransactionDTO> result = transactionService.updateTransaction(1L, updateRequest);

        assertTrue(result.isPresent());
        assertEquals("111111111", result.get().getSenderAccountNumber());
        assertEquals("222222222", result.get().getReceiverAccountNumber());
        assertEquals(200.0, result.get().getAmount());
        assertEquals(originalTimestamp, result.get().getTimestamp());
    }

    @Test
    void updateTransaction_WhenNotFound_ShouldReturnEmptyOptional() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TransactionDTO> result = transactionService.updateTransaction(99L, transactionDTO);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteTransaction_WhenExists_ShouldReturnTrue() {
        when(transactionRepository.existsById(1L)).thenReturn(true);

        boolean result = transactionService.deleteTransaction(1L);

        assertTrue(result);
        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void deleteTransaction_WhenNotExists_ShouldReturnFalse() {
        when(transactionRepository.existsById(99L)).thenReturn(false);

        boolean result = transactionService.deleteTransaction(99L);

        assertFalse(result);
        verify(transactionRepository, never()).deleteById(anyLong());
    }
}
