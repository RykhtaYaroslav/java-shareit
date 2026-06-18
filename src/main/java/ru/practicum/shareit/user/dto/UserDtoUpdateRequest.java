package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserDtoUpdateRequest {
    private String name;

    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Некорректный формат email")
    private String email;
}
