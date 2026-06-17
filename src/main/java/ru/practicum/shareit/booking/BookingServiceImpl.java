package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingUpdateDto;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(Long userId, BookingRequestDto bookingRequestDto) {
        /**TODO:
         * check user existence +
         * check item existence +
         * check user is not owner of item +
         * check item availability +
         * check booking dates +
         * create booking with WAITING status
         * save in repository and give back DTO
         */

        User user = getBooker(userId);

        Item item = extractItem(bookingRequestDto, user);

        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        checkBookingDates(start, end, item);

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .startDate(start)
                .endDate(end)
                .status(BookingStatus.WAITING)
                .build();

        booking = bookingRepository.save(booking);

        return BookingDto.mapToDto(booking);
    }

    @Override
    public BookingDto update(BookingUpdateDto bookingUpdateDto) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    private Item extractItem(BookingRequestDto bookingRequestDto, User user){
        Item item = itemRepository.findById(bookingRequestDto.getItemId()).orElseThrow(
                () -> new NotFoundException(String.format("Предмет с id=%d не найден", bookingRequestDto.getItemId())));

        if (user.getId().equals(item.getOwnerId())) {
            throw new NotFoundException(String.format(
                    "Пользователь с id=%d является владельцем предмета с id=%d и не может его забронировать",
                    user.getId(), item.getId()));
        }

        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new NotAvailableException("Предмет не доступен");
        }

        return item;
    }

    private User getBooker(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));
    }

    private void checkBookingDates(LocalDateTime start, LocalDateTime end, Item item) {
        if (!start.isBefore(end)) {
            throw new NotAvailableException("Выбран некорректный срок аренды");
        }
        List<Booking> intersections = bookingRepository.findBookingInPeriod(item.getId(), start, end, List.of(BookingStatus.WAITING, BookingStatus.APPROVED));
        if (!intersections.isEmpty()) {
            throw new NotAvailableException("На выбранный период времени предмет уже забронирован");
        }
    }
}
