package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.IllegalBookingStateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BookingServiceGetUserBookingsUnitTest {
    @Mock
    private BookingRepository bookingRepositoryMock;

    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private static final Long USER_ID = 1L;
    private final Long itemId = 1L;
    private final Long bookingId = 1L;

    private final User booker = User.builder().id(USER_ID).name("Booker").build();
    private final Item item = new Item(itemId, 2L, "Дрель", "Простая дрель", true, null);

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

    @DisplayName("Успешное получение списка бронирований пользователя для различных состояний")
    @ParameterizedTest(name = "Состояние: {0}")
    @MethodSource("provideBookingStatesAndMockSetup")
    void shouldReturnUserBookingsForEveryState(String stateString, BiConsumer<BookingRepository, List<Booking>> setupMockBehavior, int verifyMethodIndex) {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking testBooking = createTestBooking();
        List<Booking> repositoryResult = List.of(testBooking);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя в системе во всех кейсах
        Mockito.when(userRepositoryMock.findById(USER_ID)).thenReturn(Optional.of(booker));
        // Применяем специфичную для каждого состояния настройку репозитория бронирований через консьюмер
        setupMockBehavior.accept(bookingRepositoryMock, repositoryResult);

        // ВЫЗОВ МЕТОДА
        //
        List<BookingDto> result = bookingService.getUserBookings(USER_ID, stateString);
        List<BookingDto> expected = List.of(BookingDto.mapToDto(testBooking));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).hasSize(1).isEqualTo(expected);
        // Верифицируем, что до userRepository дошли ровно один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(USER_ID);
        // Верифицируем вызов правильного метода в bookingRepositoryMock
        verifyBookingRepositoryMethod(verifyMethodIndex);
        // Больше не обращались к методам этих репозиториев
        Mockito.verifyNoMoreInteractions(userRepositoryMock, bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашивающий пользователь не существует")
    @Test
    void throwExceptionWhenUserNotFoundDuringGetBookings() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongUserId = -1L;
        String state = "ALL";

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(wrongUserId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.getUserBookings(wrongUserId, state))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongUserId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongUserId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка IllegalBookingStateException, если передано неизвестное состояние бронирования")
    @Test
    void throwExceptionWhenStateIsUnknown() {
        // ПОДГОТОВКА ДАННЫХ
        //
        String unknownState = "unsupported_state";

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(USER_ID)).thenReturn(Optional.of(booker));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.getUserBookings(USER_ID, unknownState))
                .isInstanceOf(IllegalBookingStateException.class)
                .hasMessage(String.format("Unknown state: %s", unknownState));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(USER_ID);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    private static Stream<Arguments> provideBookingStatesAndMockSetup() {
        return Stream.of(
                Arguments.of("ALL", (BiConsumer<BookingRepository, List<Booking>>) (mock, res) ->
                        Mockito.when(mock.findAllByBookerIdOrderByStartDateDesc(USER_ID)).thenReturn(res), 1),
                Arguments.of("CURRENT", (BiConsumer<BookingRepository, List<Booking>>) (mock, res) ->
                        Mockito.when(mock.findAllByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Mockito.eq(USER_ID), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(res), 2),
                Arguments.of("PAST", (BiConsumer<BookingRepository, List<Booking>>) (mock, res) ->
                        Mockito.when(mock.findAllByBookerIdAndEndDateBeforeOrderByStartDateDesc(Mockito.eq(USER_ID), Mockito.any(LocalDateTime.class))).thenReturn(res), 3),
                Arguments.of("FUTURE", (BiConsumer<BookingRepository, List<Booking>>) (mock, res) ->
                        Mockito.when(mock.findAllByBookerIdAndStartDateAfterOrderByStartDateDesc(Mockito.eq(USER_ID), Mockito.any(LocalDateTime.class))).thenReturn(res), 4),
                Arguments.of("WAITING", (BiConsumer<BookingRepository, List<Booking>>) (mock, res) ->
                        Mockito.when(mock.findAllByBookerIdAndStatusOrderByStartDateDesc(USER_ID, BookingStatus.WAITING)).thenReturn(res), 5),
                Arguments.of("REJECTED", (BiConsumer<BookingRepository, List<Booking>>) (mock, res) ->
                        Mockito.when(mock.findAllByBookerIdAndStatusOrderByStartDateDesc(USER_ID, BookingStatus.REJECTED)).thenReturn(res), 6)
        );
    }

    private void verifyBookingRepositoryMethod(int index) {
        switch (index) {
            case 1 ->
                    Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findAllByBookerIdOrderByStartDateDesc(USER_ID);
            case 2 ->
                    Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findAllByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Mockito.eq(USER_ID), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class));
            case 3 ->
                    Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findAllByBookerIdAndEndDateBeforeOrderByStartDateDesc(Mockito.eq(USER_ID), Mockito.any(LocalDateTime.class));
            case 4 ->
                    Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findAllByBookerIdAndStartDateAfterOrderByStartDateDesc(Mockito.eq(USER_ID), Mockito.any(LocalDateTime.class));
            case 5 ->
                    Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findAllByBookerIdAndStatusOrderByStartDateDesc(USER_ID, BookingStatus.WAITING);
            case 6 ->
                    Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findAllByBookerIdAndStatusOrderByStartDateDesc(USER_ID, BookingStatus.REJECTED);
        }
    }
}