package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BookingServiceApproveUnitTest {
    @Mock
    private BookingRepository bookingRepositoryMock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final Long ownerId = 1L;
    private final Long wrongUserId = -1L;
    private final Long bookerId = 3L;
    private final Long itemId = 1L;
    private final Long bookingId = 1L;

    private final User booker = User.builder().id(bookerId).name("Booker").build();
    private final Item item = new Item(itemId, ownerId, "Дрель", "Простая дрель", true, null);

    private final LocalDateTime baseTime = LocalDateTime.now();

    private Booking createWaitingBooking() {
        return Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .startDate(baseTime.plusDays(1))
                .endDate(baseTime.plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
    }

    @DisplayName("Успешное подтверждение бронирования владельцем вещи")
    @Test
    void shouldApproveBookingSuccessfully() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking waitingBooking = createWaitingBooking();
        boolean approvedFlag = true;

        Booking approvedBooking = createWaitingBooking();
        approvedBooking.setStatus(BookingStatus.APPROVED);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим бронирование в статусе WAITING
        Mockito.when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(waitingBooking));
        // Успешно сохраняем обновлённое бронирование
        Mockito.when(bookingRepositoryMock.save(Mockito.any(Booking.class))).thenReturn(approvedBooking);

        // ВЫЗОВ МЕТОДА
        //
        BookingDto result = bookingService.approve(ownerId, bookingId, approvedFlag);
        BookingDto expected = BookingDto.mapToDto(approvedBooking);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Обратились в bookingRepositoryMock.findById только один раз
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(bookingId);
        // Обратились в bookingRepositoryMock.save только один раз
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).save(Mockito.any(Booking.class));
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }

    @DisplayName("Успешное отклонение бронирования владельцем вещи")
    @Test
    void shouldRejectBookingSuccessfully() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking waitingBooking = createWaitingBooking();
        boolean approvedFlag = false;

        Booking rejectedBooking = createWaitingBooking();
        rejectedBooking.setStatus(BookingStatus.REJECTED);

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(waitingBooking));
        Mockito.when(bookingRepositoryMock.save(Mockito.any(Booking.class))).thenReturn(rejectedBooking);

        // ВЫЗОВ МЕТОДА
        //
        BookingDto result = bookingService.approve(ownerId, bookingId, approvedFlag);
        BookingDto expected = BookingDto.mapToDto(rejectedBooking);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(bookingId);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).save(Mockito.any(Booking.class));
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если обрабатываемое бронирование не найдено в базе")
    @Test
    void throwExceptionWhenBookingNotFoundDuringApprove() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongBookingId = -1L;

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(bookingRepositoryMock.findById(wrongBookingId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.approve(ownerId, wrongBookingId, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Бронирование с id=%d не найдено", wrongBookingId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(wrongBookingId);
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotAvailableException, если статус пытается изменить не владелец вещи")
    @Test
    void throwExceptionWhenUserIsNotItemOwner() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking waitingBooking = createWaitingBooking();

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(waitingBooking));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.approve(wrongUserId, bookingId, true))
                .isInstanceOf(NotAvailableException.class)
                .hasMessage(String.format("Пользователь с id=%d не является владельцем вещи с id=%d. Доступ к бронированию ограничен", wrongUserId, itemId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(bookingId);
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotAvailableException, если бронирование уже было обработано ранее")
    @Test
    void throwExceptionWhenBookingStatusIsAlreadyProcessed() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking alreadyApprovedBooking = createWaitingBooking();
        alreadyApprovedBooking.setStatus(BookingStatus.APPROVED);

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(alreadyApprovedBooking));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.approve(ownerId, bookingId, true))
                .isInstanceOf(NotAvailableException.class)
                .hasMessage(String.format("Бронирование с id=%d уже обработано. Текущий статус: %s", bookingId, BookingStatus.APPROVED));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(bookingId);
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }
}