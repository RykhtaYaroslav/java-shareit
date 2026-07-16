package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoCreateRequest;
import ru.practicum.shareit.booking.dto.BookingDtoUpdateRequest;

import java.util.List;

@SuppressWarnings("unused")
public interface BookingService {
    BookingDto create(Long userId, BookingDtoCreateRequest bookingDtoCreateRequest);

    BookingDto update(BookingDtoUpdateRequest bookingDtoUpdateRequest);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long userId, String state);

    void delete(Long id);

    List<BookingDto> getOwnerBookings(Long ownerId, String state);
}
