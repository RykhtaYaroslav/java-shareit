package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingUpdateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.IllegalBookingStateException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(Long userId, BookingRequestDto bookingRequestDto) {
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
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с id=%d не найдено", bookingId)));

        if (!booking.getItem().getOwnerId().equals(userId)) {
            throw new NotFoundException(String.format(
                    "Пользователь с id=%d не является владельцем вещи с id=%d. Доступ к бронированию ограничен",
                    userId, booking.getItem().getId()));
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new NotAvailableException(String.format(
                    "Бронирование с id=%d уже обработано. Текущий статус: %s", bookingId, booking.getStatus()));
        }

        booking.setStatus(Boolean.TRUE.equals(approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return BookingDto.mapToDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с id=%d не найдено", bookingId)));

        if (!booking.getItem().getOwnerId().equals(userId) || !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException(String.format(
                    "Пользователь с id=%d не является владельцем вещи или автором бронирования с id = %d. Доступ к бронированию ограничен",
                    userId, booking.getId()));
        }

        return BookingDto.mapToDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String stateString) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));

        BookingState state;

        try {
            state = BookingState.valueOf(stateString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalBookingStateException(String.format("Unknown state: %s", stateString));
        }

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByBookerIdOrderByStartDateDesc(userId);
            case CURRENT ->
                    bookings = bookingRepository.findAllByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(userId, now, now);
            case PAST ->
                    bookings = bookingRepository.findAllByBookerIdAndEndDateBeforeOrderByStartDateDesc(userId, now);
            case FUTURE ->
                    bookings = bookingRepository.findAllByBookerIdAndStartDateAfterOrderByStartDateDesc(userId, now);
            case WAITING ->
                    bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.REJECTED);
            default -> throw new IllegalBookingStateException("Unexpected value: " + state);
        }
        return bookings.stream().map(BookingDto::mapToDto).toList();
    }

    @Override
    public void delete(Long id) {

    }

    private Item extractItem(BookingRequestDto bookingRequestDto, User user) {
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
