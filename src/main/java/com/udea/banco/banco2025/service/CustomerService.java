package com.udea.banco.banco2025.service;

import com.udea.banco.banco2025.DTO.CustomerDTO;
import com.udea.banco.banco2025.entity.Customer;
import com.udea.banco.banco2025.mapper.CustomerMapper;
import com.udea.banco.banco2025.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDTO)
                .toList();
    }

    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Cliente no Encontrado"));
    }

    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Customer customer = customerMapper.toEntity(customerDTO);
        return customerMapper.toDTO(customerRepository.save(customer));
    }

    public java.util.Optional<CustomerDTO> updateCustomer(Long id, CustomerDTO customerDTO) {
        return customerRepository.findById(id).map(existing -> {
            if (customerDTO.getFirstName() != null) {
                existing.setFirstName(customerDTO.getFirstName());
            }
            if (customerDTO.getLastName() != null) {
                existing.setLastName(customerDTO.getLastName());
            }
            if (customerDTO.getAccountNumber() != null) {
                existing.setAccountNumber(customerDTO.getAccountNumber());
            }
            if (customerDTO.getBalance() != null) {
                existing.setBalance(customerDTO.getBalance());
            }
            Customer updated = customerRepository.save(existing);
            return customerMapper.toDTO(updated);
        });
    }

    public boolean deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
