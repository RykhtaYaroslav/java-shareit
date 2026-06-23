package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@SuppressWarnings("unused")
public interface ItemService {
    ItemDto create(Long userId, ItemDtoCreateRequest request);

    ItemDto update(Long userId, Long itemId, ItemDtoUpdateRequest request);

    ItemDto findById(Long userId, Long itemId);

    List<ItemDto> findAllByOwnerId(Long userId);

    List<ItemDto> searchByNameOrDescription(String text);

    Item getItem(Long itemId);

    List<Item> findAllByRequestsIds(List<Long> requestIds);
}
