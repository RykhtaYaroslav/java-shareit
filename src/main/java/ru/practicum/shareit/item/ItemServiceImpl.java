package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto create(Long userId, ItemDtoCreateRequest request) {
        checkUserExistence(userId);

        Item itemRequest = ItemDtoCreateRequest.mapToModel(userId, request);

        Item item = itemRepository.save(itemRequest);
        return ItemDto.mapToDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDtoUpdateRequest request) {
        checkUserExistence(userId);

        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("Предмет с id=%d не найден", itemId)));

        updateItemFields(item, request, userId, itemId);

        itemRepository.save(item);
        return ItemDto.mapToDto(item);
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        checkUserExistence(userId);

        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("Предмет с id=%d не найден", itemId)));

        if (item.getOwnerId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> previousBookingsForItems = bookingRepository.findPreviousBookingsForItems(List.of(itemId), now);
            List<Booking> featureBookingsForItems = bookingRepository.findFeatureBookingsForItems(List.of(itemId), now);

            Booking last = previousBookingsForItems.isEmpty() ? null : previousBookingsForItems.getFirst();
            Booking next = featureBookingsForItems.isEmpty() ? null : featureBookingsForItems.getFirst();

            return ItemDto.mapToDto(item, last, next);
        } else {
            return ItemDto.mapToDto(item);
        }
    }

    @Override
    public List<ItemDto> findAllByOwnerId(Long userId) {
        checkUserExistence(userId);

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        LocalDateTime now = LocalDateTime.now();

        List<Booking> previousBookingsForItems = bookingRepository.findPreviousBookingsForItems(itemIds, now);
        List<Booking> featureBookingsForItems = bookingRepository.findFeatureBookingsForItems(itemIds, now);

        Map<Long, Booking> previousBookingsMap = previousBookingsForItems.stream().collect(Collectors.toMap(
                booking -> booking.getItem().getId(),
                booking -> booking,
                (existing, replacement) -> existing));

        Map<Long, Booking> featureBookingsMap = featureBookingsForItems.stream().collect(Collectors.toMap(
                booking -> booking.getItem().getId(),
                booking -> booking,
                (existing, replacement) -> existing));


        return items.stream()
                .map(item -> {
                    Booking last = previousBookingsMap.getOrDefault(item.getId(), null);
                    Booking next = featureBookingsMap.getOrDefault(item.getId(), null);
                    return ItemDto.mapToDto(item, last, next);
                })
                .toList();
    }

    @Override
    public List<ItemDto> searchByNameOrDescription(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.findAllByNameOrDescriptionContainingIgnoreCase(text);

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
