package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long id;
    private ItemShortDto item;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookerShortDto booker;
    private String status;

    public static BookingDto mapToDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingDto.builder()
                .id(booking.getId())
                .item(booking.getItem() != null ? ItemShortDto.mapToDto(booking.getItem()) : null)
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .booker(booking.getBooker() != null ? BookerShortDto.mapToDto(booking.getBooker()) : null)
                .status(booking.getStatus() != null ? booking.getStatus().toString() : null)
                .build();
    }

    @Data
    @Builder
    public static class BookerShortDto {
        private Long id;

        public static BookerShortDto mapToDto(User user) {
            return BookerShortDto.builder()
                    .id(user.getId())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ItemShortDto {
        private Long id;
        private String name;

        public static ItemShortDto mapToDto(Item item) {
            return ItemShortDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .build();
        }
    }
}
