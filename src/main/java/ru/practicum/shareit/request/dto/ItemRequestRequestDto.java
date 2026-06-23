package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequestRequestDto {
    @NotBlank(message = "Description must not be empty")
    private String description;

    public ItemRequest mapToModel(ItemRequestRequestDto dto, LocalDateTime created, User user) {
        return ItemRequest.builder()
                .description(dto.description)
                .created(created)
                .user(user)
                .build();
    }
}
