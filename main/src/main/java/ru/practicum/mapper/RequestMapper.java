package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.Request;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface RequestMapper {

    @Mapping(target = "requester", source = "request.requester.id")
    @Mapping(target = "event", source = "request.event.id")
    ParticipationRequestDto toDto(Request request);

    @Mapping(target = "requester.id", source = "requester")
    @Mapping(target = "event.id", source = "event")
    Request toEntity(ParticipationRequestDto dto);
}
