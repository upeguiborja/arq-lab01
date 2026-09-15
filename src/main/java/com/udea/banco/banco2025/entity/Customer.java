package com.udea.banco.banco2025.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
public class Customer {
    // POJO

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Getter
    @Setter
    @Column(nullable = false, length = 50)
    private String firstName;

    @Getter
    @Setter
    @Column(nullable = false, length = 50)
    private String lastName;

    @Getter
    @Setter
    @Column(nullable = false)
    private Double balance;

    public Customer() {
    }

    @JsonCreator
    public Customer(@JsonProperty("id") Long id, @JsonProperty("accountNumber") String accountNumber, @JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName, @JsonProperty("balance") Double balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = balance;
    }

}
