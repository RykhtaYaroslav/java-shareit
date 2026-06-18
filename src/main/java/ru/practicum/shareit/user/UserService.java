package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

import java.util.List;

public interface UserService {
    UserDto create(UserDtoCreateRequest createRequest);

    UserDto update(Long userId, UserDtoUpdateRequest updateRequest);

    void deleteById(Long userId);

    UserDto findById(Long userId);

    List<UserDto> findAll();
}
