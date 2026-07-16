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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BookingServiceGetBookingByIdUnitTest {
    @Mock
    private BookingRepository bookingRepositoryMock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final Long ownerId = 1L;
    private final Long bookerId = 2L;
    private final Long wrongUserId = -1L;
    private final Long itemId = 1L;
    private final Long bookingId = 1L;

    private final User booker = User.builder().id(bookerId).name("Booker").build();
    private final Item item = new Item(itemId, ownerId, "Дрель", "Простая дрель", true, null);

    private Booking createTestBooking() {
        return Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
    }

    @DisplayName("Успешное получение бронирования автором этого бронирования")
    @Test
    void shouldReturnBookingToBookerSuccessfully() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking booking = createTestBooking();

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим бронирование в репозитории
        Mockito.when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(booking));

        // ВЫЗОВ МЕТОДА
        //
        BookingDto result = bookingService.getBookingById(bookerId, bookingId);
        BookingDto expected = BookingDto.mapToDto(booking);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Обратились в bookingRepositoryMock.findById только один раз
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(bookingId);
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }

    @DisplayName("Успешное получение бронирования владельцем забронированной вещи")
    @Test
    void shouldReturnBookingToItemOwnerSuccessfully() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking booking = createTestBooking();

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(booking));

        // ВЫЗОВ МЕТОДА
        //
        BookingDto result = bookingService.getBookingById(ownerId, bookingId);
        BookingDto expected = BookingDto.mapToDto(booking);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(bookingId);
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашиваемое бронирование отсутствует в базе данных")
    @Test
    void throwExceptionWhenBookingNotFoundById() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongBookingId = -1L;

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(bookingRepositoryMock.findById(wrongBookingId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.getBookingById(bookerId, wrongBookingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Бронирование с id=%d не найдено", wrongBookingId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(wrongBookingId);
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если к бронированию обращается сторонний пользователь (не автор и не владелец)")
    @Test
    void throwExceptionWhenUserHasNoAccessToBooking() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking booking = createTestBooking();

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(booking));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.getBookingById(wrongUserId, bookingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format(
                        "Пользователь с id=%d не является владельцем вещи или автором бронирования с id = %d. Доступ к бронированию ограничен",
                        wrongUserId, bookingId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findById(bookingId);
        Mockito.verifyNoMoreInteractions(bookingRepositoryMock);
    }
}