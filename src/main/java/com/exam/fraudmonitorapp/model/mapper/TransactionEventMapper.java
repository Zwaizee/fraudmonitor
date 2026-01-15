package com.exam.fraudmonitorapp.model.mapper;

import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import com.exam.fraudmonitorapp.model.dto.TransactionEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TransactionEventMapper {
    TransactionEventMapper INSTANCE = Mappers.getMapper(TransactionEventMapper.class );

    TransactionEventDto toDto(TransactionEventEntity entity);

    TransactionEventEntity fromDto(TransactionEventDto dto);

    TransactionEventDto toUpdateDto(TransactionEventEntity entity, @MappingTarget TransactionEventDto dto);

    TransactionEventEntity fromDtoUpdate(TransactionEventDto dto, @MappingTarget TransactionEventEntity entity);
}
