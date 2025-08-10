package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.EndpointHitDto;
import ru.practicum.model.EndpointHit;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface EndpointHitMapper {

    @Mapping(target = "id", ignore = true)
    EndpointHit toEntity(EndpointHitDto dto);
}