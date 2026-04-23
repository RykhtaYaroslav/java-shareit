package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    public ItemDto create(Long userId, ItemDtoCreateRequest request) {
        checkUserExistence(userId);

        Item itemRequest = ItemMapper.mapToModel(userId, request);

        Item item = repository.create(itemRequest);
        return ItemMapper.mapToDto(item);
    }

    public ItemDto update(Long userId, Long itemId, ItemDtoUpdateRequest request) {
        checkUserExistence(userId);

        Item itemRequest = ItemMapper.mapToModel(itemId, userId, request);

        Item item = repository.update(itemRequest);
        return ItemMapper.mapToDto(item);
    }

    public ItemDto findById(Long itemId) {
            Optional<Item> itemOptional = repository.findById(itemId);

            if (itemOptional.isEmpty()) {
                throw new NotFoundException(String.format("Предмет с id=%d не найден", itemId));
            } else {
                Item item = itemOptional.get();

                return ItemMapper.mapToDto(item);
            }
    }

    public List<ItemDto> findAllByUserId(Long userId){
        checkUserExistence(userId);

        List<Item> items = repository.findAllByUserId(userId);

        return items.stream()
                .map(ItemMapper::mapToDto)
                .toList();
    }

    public List<ItemDto> search(String text){
        if (text.isBlank()){
            return Collections.emptyList();
        }

        List<Item> items = repository.search(text);

        return items.stream()
                .map(ItemMapper::mapToDto)
                .toList();
    }

    private void checkUserExistence(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }
    }
}
