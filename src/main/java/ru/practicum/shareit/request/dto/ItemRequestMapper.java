package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "id", ignore = true)
    ItemRequest mapToModel(ItemRequestRequestDto dto, LocalDateTime created, User user);

    ItemRequestDto mapToDto(ItemRequest itemRequest, List<ItemRequestDto.ItemResponseDto> items);

    ItemRequestDto.ItemResponseDto mapToItemResponseDto(Item item);
}
