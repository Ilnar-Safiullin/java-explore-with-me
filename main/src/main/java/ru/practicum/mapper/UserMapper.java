package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.user.RequestUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(RequestUserDto newUserDto);
}