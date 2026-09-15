package com.udea.banco.banco2025.controller;

import com.udea.banco.banco2025.DTO.TransactionDTO;
import com.udea.banco.banco2025.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionFacade;

    @Autowired
    public TransactionController(TransactionService transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    // Obtener todas las transacciones
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionFacade.getAllTransactions());
    }

    // Obtener una transacción por Id
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        return transactionFacade.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear una nueva transacción
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDTO) {
        if (transactionDTO.getAmount() == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        return ResponseEntity.ok(transactionFacade.createTransaction(transactionDTO));
    }

    // Actualizar una transacción existente
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @RequestBody TransactionDTO transactionDTO) {
        return transactionFacade.updateTransaction(id, transactionDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar una transacción por Id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        if (transactionFacade.deleteTransaction(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
