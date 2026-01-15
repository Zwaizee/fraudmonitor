package com.exam.fraudmonitorapp.model.mapper;

import com.exam.fraudmonitorapp.model.FraudAlertEntity;
import com.exam.fraudmonitorapp.model.dto.FraudAlertDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FraudAlertMapper {
    FraudAlertMapper INSTANCE = Mappers.getMapper(FraudAlertMapper.class );

    FraudAlertDto toDto(FraudAlertEntity entity);

    FraudAlertEntity fromDto(FraudAlertDto dto);

    FraudAlertDto toUpdateDto(FraudAlertEntity entity, @MappingTarget FraudAlertDto dto);

    FraudAlertEntity fromDtoUpdate(FraudAlertDto dto, @MappingTarget FraudAlertEntity entity);
}
