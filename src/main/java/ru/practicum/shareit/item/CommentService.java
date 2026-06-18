package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public CommentDto createComment(Long userId, Long itemId, CommentRequestDto request) {
        Item item = getItem(itemId);

        User author = getUser(userId);

        LocalDateTime now = LocalDateTime.now();

        checkUserWasBooker(userId, itemId, now);

        Comment comment = commentRepository.save(CommentRequestDto.mapToModel(request, item, author, now));

        return CommentDto.mapToDto(comment);
    }

    private void checkUserWasBooker(Long userId, Long itemId, LocalDateTime now) {
        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndDateBefore(userId, itemId, BookingStatus.APPROVED, now)) {
            throw new NotAvailableException(String.format(
                    "Пользователь с id=%d не может оставить отзыв на вещь с id=%d, так как не арендовал её.", userId, itemId));
        }
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(String.format("Предмет с id=%d не найден", itemId)));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));
    }
}
