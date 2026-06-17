package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Booking;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestDto {
    @NotNull(message = "itemId не может быть null")
    private Long itemId;

    @NotNull(message = "start не может быть null")
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "end не может быть null")
    @FutureOrPresent(message = "Дата окончания бронирования не может быть в прошлом")
    private LocalDateTime end;

    public static Booking mapToModel (BookingRequestDto bookingRequestDto){
        return Booking.builder()
                .id(bookingRequestDto.getItemId())
                .startDate(bookingRequestDto.getStart())
                .endDate(bookingRequestDto.getEnd())
                .build();
    }
}
