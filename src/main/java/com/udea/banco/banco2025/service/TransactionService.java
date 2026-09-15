package com.udea.banco.banco2025.service;

import com.udea.banco.banco2025.DTO.TransactionDTO;
import com.udea.banco.banco2025.entity.Customer;
import com.udea.banco.banco2025.entity.Transaction;
import com.udea.banco.banco2025.mapper.TransactionMapper;
import com.udea.banco.banco2025.repository.CustomerRepository;
import com.udea.banco.banco2025.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final TransactionMapper transactionMapper;

    @Autowired
    public TransactionService(
            TransactionRepository transactionRepository,
            CustomerRepository customerRepository,
            TransactionMapper transactionMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.transactionMapper = transactionMapper;
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toDTO)
                .toList();
    }

    public Optional<TransactionDTO> getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toDTO);
    }

    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        return transferMoney(transactionDTO);
    }

    public TransactionDTO transferMoney(TransactionDTO transactionDTO) {
        if (transactionDTO.getSenderAccountNumber() == null || transactionDTO.getReceiverAccountNumber() == null) {
            throw new IllegalArgumentException("Sender Account Number or Receiver Account Number cannot be null");
        }

        if (transactionDTO.getAmount() == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        Customer sender = customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Sender Account Number not found"));

        Customer receiver = customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Receiver Account Number not found"));

        // Validar que el remitente tenga saldo suficiente
        if (sender.getBalance() < transactionDTO.getAmount()) {
            throw new IllegalArgumentException("Sender Balance not enough");
        }

        // Realiza la transferencia
        sender.setBalance(sender.getBalance() - transactionDTO.getAmount());
        receiver.setBalance(receiver.getBalance() + transactionDTO.getAmount());

        // Guardar los cambios en las cuentas
        customerRepository.save(sender);
        customerRepository.save(receiver);

        // Crear y guardar la transacción
        Transaction transaction = transactionMapper.toEntity(transactionDTO);
        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(LocalDateTime.now());
        }
        transaction = transactionRepository.save(transaction);

        // Devolver la transacción creada como un DTO
        return transactionMapper.toDTO(transaction);
    }

    public Optional<TransactionDTO> updateTransaction(Long id, TransactionDTO transactionDTO) {
        return transactionRepository.findById(id).map(existingTransaction -> {
            if (transactionDTO.getSenderAccountNumber() != null) {
                existingTransaction.setSenderAccountNumber(transactionDTO.getSenderAccountNumber());
            }
            if (transactionDTO.getReceiverAccountNumber() != null) {
                existingTransaction.setReceiverAccountNumber(transactionDTO.getReceiverAccountNumber());
            }
            if (transactionDTO.getAmount() != null) {
                existingTransaction.setAmount(transactionDTO.getAmount());
            }
            if (transactionDTO.getTimestamp() != null) {
                existingTransaction.setTimestamp(transactionDTO.getTimestamp());
            } else if (existingTransaction.getTimestamp() == null) {
                existingTransaction.setTimestamp(LocalDateTime.now());
            }
            Transaction updated = transactionRepository.save(existingTransaction);
            return transactionMapper.toDTO(updated);
        });
    }

    public boolean deleteTransaction(Long id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<TransactionDTO> getTransactionsForAccount(String accountNumber) {
        List<Transaction> transactions = transactionRepository
                .findBySenderAccountNumberOrReceiverAccountNumber(accountNumber, accountNumber);

        return transactions
                .stream()
                .map(transactionMapper::toDTO)
                .toList();
    }
}
