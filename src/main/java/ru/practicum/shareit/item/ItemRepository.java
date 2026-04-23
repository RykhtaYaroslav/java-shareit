package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item create(Item itemCreateRequest);

    Item update(Item itemUpdateRequest);

    Optional<Item> findById(Long itemId);

    List<Item> findAllByUserId(Long userId);

    List<Item> search(String text);
}
