package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingUpdateDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingRequestDto bookingRequestDto);

    BookingDto update(BookingUpdateDto bookingUpdateDto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long userId, String state);

    void delete(Long id);

    List<BookingDto> getOwnerBookings(Long ownerId, String state);
}
