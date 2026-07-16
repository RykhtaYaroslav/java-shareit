package ru.practicum.shareit.user.dto;

import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
public class UserDtoUpdateRequest {
    private String name;

    private String email;

    public static User mapToModel(Long userId, UserDtoUpdateRequest updateRequest) {
        return User.builder()
                .id(userId)
                .name(updateRequest.getName())
                .email(updateRequest.getEmail())
                .build();
    }
}
