package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDtoCreateRequest request) {
        checkUserExistence(userId);

        Item itemRequest = ItemDtoCreateRequest.mapToModel(userId, request);

        Item item = repository.save(itemRequest);
        return ItemDto.mapToDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDtoUpdateRequest request) {
        checkUserExistence(userId);

        Item itemRequest = ItemDtoUpdateRequest.mapToModel(itemId, userId, request);

        Item item = repository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("Предмет с id=%d не найден", itemId)));

        updateItemFields(item, request, userId, itemId);

        repository.save(itemRequest);
        return ItemDto.mapToDto(item);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Optional<Item> itemOptional = repository.findById(itemId);

        if (itemOptional.isEmpty()) {
            throw new NotFoundException(String.format("Предмет с id=%d не найден", itemId));
        } else {
            Item item = itemOptional.get();

            return ItemDto.mapToDto(item);
        }
    }

    @Override
    public List<ItemDto> findAllByOwnerId(Long userId) {
        checkUserExistence(userId);

        List<Item> items = repository.findAllByOwnerId(userId);

        return items.stream()
                .map(ItemDto::mapToDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchByNameOrDescription(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = repository.findAllByNameOrDescriptionContainingIgnoreCase(text);

        return items.stream()
                .map(ItemDto::mapToDto)
                .toList();
    }

    private void checkUserExistence(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }
    }

    private void updateItemFields(Item oldItem, ItemDtoUpdateRequest request, Long userId, Long itemId) {
        if (!oldItem.getOwnerId().equals(userId)) {
            throw new NotFoundException(String.format("У пользователя с id=%d нет доступа к редактированию предмета с id=%d", userId, itemId));
        }

        if (request.getName() != null) {
            oldItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            oldItem.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            oldItem.setAvailable(request.getAvailable());
        }
    }
}
