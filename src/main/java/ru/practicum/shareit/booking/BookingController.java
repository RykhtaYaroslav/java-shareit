package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.create(userId, bookingRequestDto);
    }

}
