package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
@Builder
public class UserDtoCreateRequest {
    private String name;
    private String email;

    public static User mapToModel(UserDtoCreateRequest createRequest) {
        return User.builder()
                .name(createRequest.getName())
                .email(createRequest.getEmail())
                .build();
    }
}
