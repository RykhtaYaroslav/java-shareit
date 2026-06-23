package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;

    public static UserDto mapToDto(User user) {
        return ru.practicum.shareit.user.dto.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
