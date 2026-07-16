package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequestDtoCreateRequest {
    private String description;

    public ItemRequest mapToModel(ItemRequestDtoCreateRequest dto, LocalDateTime created, User user) {
        return ItemRequest.builder()
                .description(dto.description)
                .created(created)
                .user(user)
                .build();
    }
}
