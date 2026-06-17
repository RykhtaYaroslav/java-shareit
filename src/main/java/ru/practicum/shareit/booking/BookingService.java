package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingUpdateDto;

@Service
public interface BookingService {
    BookingDto create(Long userId,BookingRequestDto bookingRequestDto);

    BookingDto update(BookingUpdateDto bookingUpdateDto);

    void delete(Long id);
}
