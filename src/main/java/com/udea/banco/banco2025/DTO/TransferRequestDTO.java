package com.udea.banco.banco2025.DTO;

import lombok.Data;

@Data
public class TransferRequestDTO {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private Double amount;
}
