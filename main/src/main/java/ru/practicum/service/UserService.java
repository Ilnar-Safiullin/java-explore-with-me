package ru.practicum.service;

import ru.practicum.dto.user.RequestUserDto;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUser(RequestUserDto requestUserDto);

    void deleteUser(Long userId);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);
}