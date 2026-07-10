package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDtoCreateRequest {
    private Long itemId;

    private LocalDateTime start;

    private LocalDateTime end;

    public static Booking mapToModel(BookingDtoCreateRequest bookingDtoCreateRequest) {
        return Booking.builder()
                .id(bookingDtoCreateRequest.getItemId())
                .startDate(bookingDtoCreateRequest.getStart())
                .endDate(bookingDtoCreateRequest.getEnd())
                .build();
    }
}
