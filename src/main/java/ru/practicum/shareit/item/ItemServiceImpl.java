package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Comment;
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
    private final CommentRepository commentRepository;

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
    @Transactional(readOnly = true)
    public ItemDto findById(Long userId, Long itemId) {
        checkUserExistence(userId);

        Item item = getItem(itemId);

        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);

        List<CommentDto> commentsDto = comments.stream().map(CommentDto::mapToDto).toList();

        if (item.getOwnerId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> previousBookingsForItems = bookingRepository.findPreviousBookingsForItems(List.of(itemId), now);
            List<Booking> featureBookingsForItems = bookingRepository.findFeatureBookingsForItems(List.of(itemId), now);

            Booking last = previousBookingsForItems.isEmpty() ? null : previousBookingsForItems.getFirst();
            Booking next = featureBookingsForItems.isEmpty() ? null : featureBookingsForItems.getFirst();

            return ItemDto.mapToDto(item, last, next, commentsDto);
        } else {
            return ItemDto.mapToDto(item, commentsDto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findAllByOwnerId(Long userId) {
        checkUserExistence(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, Booking> previousBookingsMap = getPreviousBookingsMap(itemIds, now);
        Map<Long, Booking> featureBookingsMap = getFeatureBookingsMap(itemIds, now);
        Map<Long, List<Comment>> commentsMap = getCommentsMap(itemIds);

        return items.stream()
                .map(item -> {
                    Booking last = previousBookingsMap.getOrDefault(item.getId(), null);
                    Booking next = featureBookingsMap.getOrDefault(item.getId(), null);
                    List<CommentDto> commentsDto = commentsMap.getOrDefault(item.getId(), Collections.emptyList()).stream().map(CommentDto::mapToDto).toList();
                    return ItemDto.mapToDto(item, last, next, commentsDto);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchByNameOrDescription(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Comment>> commentsMap = getCommentsMap(itemIds);

        return items.stream()
                .filter(Item::getAvailable)
                .map(item -> {
                    List<CommentDto> commentsDto = commentsMap.getOrDefault(item.getId(), Collections.emptyList()).stream().map(CommentDto::mapToDto).toList();
                    return ItemDto.mapToDto(item, commentsDto);
                })
                .toList();
    }

    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("Предмет с id=%d не найден", itemId)));
    }

    @Override
    public List<ItemDto> findAllByRequestId(Long requestId) {
        List<Item> items = itemRepository.findAllByRequestIdAndAvailableTrue(requestId);
        return items.stream().map(ItemDto::mapToDto).toList();
    }

    private Map<Long, Booking> getPreviousBookingsMap(List<Long> itemIds, LocalDateTime now) {
        List<Booking> previousBookingsForItems = bookingRepository.findPreviousBookingsForItems(itemIds, now);

        return previousBookingsForItems.stream().collect(Collectors.toMap(
                booking -> booking.getItem().getId(),
                booking -> booking,
                (existing, replacement) -> existing));
    }

    private Map<Long, Booking> getFeatureBookingsMap(List<Long> itemIds, LocalDateTime now) {
        List<Booking> featureBookingsForItems = bookingRepository.findFeatureBookingsForItems(itemIds, now);

        return featureBookingsForItems.stream().collect(Collectors.toMap(
                booking -> booking.getItem().getId(),
                booking -> booking,
                (existing, replacement) -> existing));
    }

    private Map<Long, List<Comment>> getCommentsMap(List<Long> itemIds) {
        List<Comment> comments = commentRepository.findAllByItemIdInOrderByCreatedDesc(itemIds);

        return comments.stream().collect(Collectors.groupingBy(
                comment -> comment.getItem().getId(),
                Collectors.toList()
        ));
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
