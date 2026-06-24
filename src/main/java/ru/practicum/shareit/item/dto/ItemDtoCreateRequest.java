package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

@Data
public class ItemDtoCreateRequest {
    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @NotBlank(message = "Поле description не может быть пустым")
    private String description;

    @NotNull(message = "Поле available не может быть пустым")
    private Boolean available;

    @Positive
    private Long itemRequestId;

    public static Item mapToModel(Long userId, ItemDtoCreateRequest request) {
        return Item.builder()
                .ownerId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .itemRequestId(request.getItemRequestId())
                .build();
    }
}
