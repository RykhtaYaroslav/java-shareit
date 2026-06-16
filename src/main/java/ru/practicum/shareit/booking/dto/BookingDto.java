package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;
    private ItemShortDto item;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BookerShortDto booker;
    private BookingStatus status;

    @Data
    public static class BookerShortDto {
        private Long id;
    }

    @Data
    public static class ItemShortDto {
        private Long id;
        private String name;
    }
}
