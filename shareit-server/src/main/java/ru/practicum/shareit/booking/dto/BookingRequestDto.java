package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestDto {
    private Long itemId;

    private LocalDateTime start;

    private LocalDateTime end;

    public static Booking mapToModel(BookingRequestDto bookingRequestDto) {
        return Booking.builder()
                .id(bookingRequestDto.getItemId())
                .startDate(bookingRequestDto.getStart())
                .endDate(bookingRequestDto.getEnd())
                .build();
    }
}
