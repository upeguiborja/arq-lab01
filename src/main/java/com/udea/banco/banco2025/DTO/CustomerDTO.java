package com.udea.banco.banco2025.DTO;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Double balance;
}
