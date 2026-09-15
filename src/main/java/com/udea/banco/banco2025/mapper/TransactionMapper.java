package com.udea.banco.banco2025.mapper;

import com.udea.banco.banco2025.DTO.TransactionDTO;
import com.udea.banco.banco2025.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    TransactionDTO toDTO(Transaction transaction);

    Transaction toEntity(TransactionDTO transactionDTO);
}