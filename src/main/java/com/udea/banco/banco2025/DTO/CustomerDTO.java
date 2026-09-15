package com.udea.banco.banco2025.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Customer data transfer object")
public class CustomerDTO {

    @Schema(description = "Unique customer ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Schema(description = "Customer first name", example = "Mateo")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Schema(description = "Customer last name", example = "Upegui")
    private String lastName;

    @NotBlank(message = "Account number is mandatory")
    @Schema(description = "Unique bank account number", example = "12356789")
    private String accountNumber;

    @NotNull(message = "Balance cannot be null")
    @PositiveOrZero(message = "Balance cannot be negative")
    @Schema(description = "Initial or current balance", example = "100000.00")
    private Double balance;
}
