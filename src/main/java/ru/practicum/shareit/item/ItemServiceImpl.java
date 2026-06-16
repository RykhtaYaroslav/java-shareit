package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    public ItemDto create(Long userId, ItemDtoCreateRequest request) {
        checkUserExistence(userId);

        Item itemRequest = ItemDtoCreateRequest.mapToModel(userId, request);

        Item item = repository.create(itemRequest);
        return ItemDto.mapToDto(item);
    }

    public ItemDto update(Long userId, Long itemId, ItemDtoUpdateRequest request) {
        checkUserExistence(userId);

        Item itemRequest = ItemDtoUpdateRequest.mapToModel(itemId, userId, request);

        checkUpdateability(itemId, userId);

        Item item = repository.update(itemRequest);
        return ItemDto.mapToDto(item);
    }

    public ItemDto findById(Long itemId) {
        Optional<Item> itemOptional = repository.findById(itemId);

        if (itemOptional.isEmpty()) {
            throw new NotFoundException(String.format("Предмет с id=%d не найден", itemId));
        } else {
            Item item = itemOptional.get();

            return ItemDto.mapToDto(item);
        }
    }

    public List<ItemDto> findAllByUserId(Long userId) {
        checkUserExistence(userId);

        List<Item> items = repository.findAllByUserId(userId);

        return items.stream()
                .map(ItemDto::mapToDto)
                .toList();
    }

    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = repository.search(text);

        return items.stream()
                .map(ItemDto::mapToDto)
                .toList();
    }

    private void checkUserExistence(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }
    }

    private void checkUpdateability(Long itemId, Long userId) {
        Item oldItem = repository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("Предмет с id=%d не найден", itemId)));

        if (!oldItem.getUserId().equals(userId)) {
            throw new NotFoundException(String.format("У пользователя с id=%d нет доступа к редактированию предмета с id=%d", userId, itemId));
        }
    }
}
