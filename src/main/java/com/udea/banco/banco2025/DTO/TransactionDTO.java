package com.udea.banco.banco2025.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Transaction data transfer object")
public class TransactionDTO {

    @Schema(description = "Unique transaction ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Sender account number is mandatory")
    @Schema(description = "Account number of the sender", example = "123456789")
    private String senderAccountNumber;

    @NotBlank(message = "Receiver account number is mandatory")
    @Schema(description = "Account number of the receiver", example = "987654321")
    private String receiverAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than zero")
    @Schema(description = "Transfer amount", example = "100.00")
    private Double amount;

    @Schema(description = "Transaction execution timestamp", example = "2026-09-19T00:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime timestamp;
}
