package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoCreateRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationTest {
    private final BookingService bookingService;

    private final EntityManager entityManager;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUsers() {
        String ownerName = "Owner";
        String bookerName = "Booker";
        String email = "@email.test";
        Long idBeforeSave = null;

        owner = new User(idBeforeSave, ownerName, ownerName + email);
        booker = new User(idBeforeSave, bookerName, bookerName + email);

        entityManager.persist(owner);
        entityManager.persist(booker);
    }

    @DisplayName("Успешное создание бронирования (create)")
    @Test
    void shouldCreateNewBooking() {
        item = createAndSaveItem(owner, owner.getName(), true);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDtoCreateRequest bookingDtoCreateRequest = BookingDtoCreateRequest.builder()
                .start(start)
                .end(end)
                .itemId(item.getId())
                .build();

        BookingDto result = bookingService.create(booker.getId(), bookingDtoCreateRequest);

        assertThat(result.getId()).isNotNull().isPositive();
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING.toString());
    }

    @DisplayName("Успешное подтверждение бронирования(approve)")
    @Test
    void shouldApproveBooking() {
        item = createAndSaveItem(owner, owner.getName(), true);

        Booking bookingBeforeApprovement = createBookingWithoutService(item, BookingStatus.WAITING);

        BookingDto bookingDtoAfterApprovement = bookingService.approve(owner.getId(), bookingBeforeApprovement.getId(), true);

        assertThat(bookingDtoAfterApprovement.getId()).isEqualTo(bookingBeforeApprovement.getId());
        assertThat(bookingDtoAfterApprovement.getStatus()).isEqualTo(BookingStatus.APPROVED.toString());
    }

    @DisplayName("Успешное нахождение booking по bookingId и booker или owner Ids (getBookingById)")
    @Test
    void shouldReturnBookingByIdAndUserId() {
        item = createAndSaveItem(owner, owner.getName(), true);

        Booking booking = createBookingWithoutService(item, BookingStatus.WAITING);
        Long bookingId = booking.getId();
        BookingDto expectedDto = BookingDto.mapToDto(booking);

        BookingDto byBookerId = bookingService.getBookingById(booker.getId(), bookingId);
        BookingDto byOwnerId = bookingService.getBookingById(owner.getId(), bookingId);

        assertThat(expectedDto).isEqualTo(byBookerId).isEqualTo(byOwnerId);
    }

    private Booking createBookingWithoutService(Item itemForBooking, BookingStatus status) {
        Long idBeforeSave = null;

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Booking booking = new Booking(idBeforeSave, itemForBooking, start, end, booker, status);

        entityManager.persist(booking);
        return booking;
    }

    private Item createAndSaveItem(User itemOwner, String name, Boolean available) {
        Item newItem = new Item(null, itemOwner.getId(), name, "Description", available, null);
        entityManager.persist(newItem);
        return newItem;
    }
}
