package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;

@Data
public class ItemDtoUpdateRequest {
    private String name;
    private String description;
    private Boolean available;

    public static Item mapToModel(Long itemId, Long userId, ItemDtoUpdateRequest request) {
        return Item.builder()
                .id(itemId)
                .ownerId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .build();
    }
}
