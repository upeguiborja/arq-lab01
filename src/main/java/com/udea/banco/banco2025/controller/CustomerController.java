package com.udea.banco.banco2025.controller;

import com.udea.banco.banco2025.DTO.CustomerDTO;
import com.udea.banco.banco2025.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Customers", description = "Operaciones relacionadas con los clientes")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerFacade;

    @Autowired
    public CustomerController(CustomerService customerFacade) {
        this.customerFacade = customerFacade;
    }

    // Obtener todos los clientes
    @Operation(summary = "Obtener todos los clientes")
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerFacade.getAllCustomers());
    }

    // Obtener un cliente por un Id
    @Operation(summary = "Obtener un cliente por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerFacade.getCustomerById(id));
    }

    // Crear un nuevo cliente
    @Operation(summary = "Crear un nuevo cliente")
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerFacade.createCustomer(customerDTO));
    }

    // Actualizar un cliente existente
    @Operation(summary = "Actualizar un cliente existente")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
        return customerFacade.updateCustomer(id, customerDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar un cliente por Id
    @Operation(summary = "Eliminar un cliente por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (customerFacade.deleteCustomer(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
