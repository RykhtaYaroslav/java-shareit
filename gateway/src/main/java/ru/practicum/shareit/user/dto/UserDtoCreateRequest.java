package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDtoCreateRequest {
    @NotBlank(message = "Поле name не может быть пустым")
    private String name;
    @NotBlank(message = "Поле email не может быть пустым")
    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Некорректный формат email")
    private String email;
}
