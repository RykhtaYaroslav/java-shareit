package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
public class UserDtoUpdateRequest {
    private String name;

    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Некорректный формат email")
    private String email;

    public static User mapToModel(Long userId, UserDtoUpdateRequest updateRequest) {
        return User.builder()
                .id(userId)
                .name(updateRequest.getName())
                .email(updateRequest.getEmail())
                .build();
    }
}
