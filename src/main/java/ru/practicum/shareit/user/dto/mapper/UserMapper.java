package ru.practicum.shareit.user.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

@UtilityClass
public class UserMapper {
    public User mapToModel(UserDtoCreateRequest createRequest) {
        return User.builder()
                .name(createRequest.getName())
                .email(createRequest.getEmail())
                .build();
    }

    public User mapToModel(Long userId, UserDtoUpdateRequest updateRequest) {
        return User.builder()
                .id(userId)
                .name(updateRequest.getName())
                .email(updateRequest.getEmail())
                .build();
    }

    public UserDto mapToDto(User user) {
        return ru.practicum.shareit.user.dto.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

}
