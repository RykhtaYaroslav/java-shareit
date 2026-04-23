package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;

@UtilityClass
public class ItemMapper {
    public static Item mapToModel(Long userId, ItemDtoCreateRequest request) {
        return Item.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .build();
    }

    public static Item mapToModel(Long itemId, Long userId, ItemDtoUpdateRequest request) {
        return Item.builder()
                .id(itemId)
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .build();
    }

    public static ItemDto mapToDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}
