package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.UserRepository;
import ru.practicum.dto.user.RequestUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional (readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDto addUser(RequestUserDto requestUserDto) {
        log.info("Создание пользователя: {}", requestUserDto);
        User user = userMapper.toEntity(requestUserDto);
        userRepository.save(user);
        log.debug("User создан: {}", user);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение юзеров с параметрами IDs: {}, from: {}, size: {}", ids, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Page<User> page = userRepository.findUsersByParam(ids, pageable);
        log.debug("Юзеры получены");
        return page.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "Id", userId));
        userRepository.deleteById(userId);
        log.debug("Удален User с Id: {}", userId);
    }
}