package com.udea.banco.banco2025.exception;

import com.udea.banco.banco2025.DTO.CustomerDTO;
import com.udea.banco.banco2025.controller.CustomerController;
import com.udea.banco.banco2025.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenValidationFails_shouldReturn400ProblemDetailWithFieldErrors() throws Exception {
        // Missing required fields: firstName, lastName, accountNumber, balance
        String invalidJson = "{}";

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.accountNumber").exists())
                .andExpect(jsonPath("$.errors.balance").exists());
    }

    @Test
    void whenNegativeBalance_shouldReturn400WithSpecificErrorMessage() throws Exception {
        String invalidJson = "{\"firstName\":\"Mateo\",\"lastName\":\"Upegui\",\"accountNumber\":\"12345\",\"balance\":-50.0}";

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.balance").value("Balance cannot be negative"));
    }

    @Test
    void whenIllegalArgument_shouldReturn400ProblemDetail() throws Exception {
        when(customerService.createCustomer(any(CustomerDTO.class)))
                .thenThrow(new IllegalArgumentException("Custom illegal argument"));

        String validJson = "{\"firstName\":\"Mateo\",\"lastName\":\"Upegui\",\"accountNumber\":\"12345\",\"balance\":100.0}";

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Custom illegal argument"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void whenDataIntegrityViolation_shouldReturn409ConflictProblemDetail() throws Exception {
        when(customerService.createCustomer(any(CustomerDTO.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '12345' for key 'customers.account_number'"));

        String validJson = "{\"firstName\":\"Mateo\",\"lastName\":\"Upegui\",\"accountNumber\":\"12345\",\"balance\":100.0}";

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Data Conflict"))
                .andExpect(jsonPath("$.detail").value("A resource with the specified unique value already exists."))
                .andExpect(jsonPath("$.status").value(409));
    }
}
