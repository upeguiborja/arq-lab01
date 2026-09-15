package com.udea.banco.banco2025.controller;

import com.udea.banco.banco2025.DTO.TransactionDTO;
import com.udea.banco.banco2025.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transactions", description = "Operaciones relacionadas con las transferencias y transacciones")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionFacade;

    @Autowired
    public TransactionController(TransactionService transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    // Obtener todas las transacciones
    @Operation(summary = "Obtener todas las transacciones")
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionFacade.getAllTransactions());
    }

    // Obtener una transacción por Id
    @Operation(summary = "Obtener una transacción por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        return transactionFacade.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear una nueva transacción
    @Operation(summary = "Crear una nueva transacción / transferir dinero")
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        return ResponseEntity.ok(transactionFacade.createTransaction(transactionDTO));
    }

    // Actualizar una transacción existente
    @Operation(summary = "Actualizar una transacción existente")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionDTO transactionDTO) {
        return transactionFacade.updateTransaction(id, transactionDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar una transacción por Id
    @Operation(summary = "Eliminar una transacción por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        if (transactionFacade.deleteTransaction(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
