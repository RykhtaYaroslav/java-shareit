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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BookingServiceGetOwnerBookingsUnitTest {
    @Mock
    private BookingRepository bookingRepositoryMock;

    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private static final Long OWNER_ID = 1L;
    private final Long itemId = 1L;
    private final Long bookingId = 1L;

    private final User owner = User.builder().id(OWNER_ID).name("Owner").build();
    private final User booker = User.builder().id(2L).name("Booker").build();
    private final Item item = new Item(itemId, OWNER_ID, "Дрель", "Простая дрель", true, null);

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

    @DisplayName("Успешное получение списка бронирований вещей владельца для различных состояний (switch-case)")
    @ParameterizedTest(name = "Состояние: {0}")
    @MethodSource("provideBookingStatesAndMockSetup")
    void shouldReturnOwnerBookingsForEveryState(String stateString, BiConsumer<BookingRepository, List<Booking>> setupMockBehavior, Consumer<BookingRepository> verifyBehavior) {
        // ПОДГОТОВКА ДАННЫХ
        //
        Booking testBooking = createTestBooking();
        List<Booking> repositoryResult = List.of(testBooking);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим владельца в системе во всех кейсах
        Mockito.when(userRepositoryMock.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        // Применяем специфичную для каждого состояния настройку репозитория через консьюмер
        setupMockBehavior.accept(bookingRepositoryMock, repositoryResult);

        // ВЫЗОВ МЕТОДА
        //
        List<BookingDto> result = bookingService.getOwnerBookings(OWNER_ID, stateString);
        List<BookingDto> expected = List.of(BookingDto.mapToDto(testBooking));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).hasSize(1).isEqualTo(expected);
        // Верифицируем, что до userRepository дошли ровно один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(OWNER_ID);
        // Выполняем специфичную верификацию, передавая туда наш живой мок из теста
        verifyBehavior.accept(bookingRepositoryMock);
        // Больше не обращались к методам этих репозиториев
        Mockito.verifyNoMoreInteractions(userRepositoryMock, bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашивающий владелец не существует")
    @Test
    void throwExceptionWhenOwnerNotFoundDuringGetBookings() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongOwnerId = -1L;
        String state = "ALL";

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(wrongOwnerId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.getOwnerBookings(wrongOwnerId, state))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongOwnerId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongOwnerId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка IllegalBookingStateException, если передано неизвестное состояние бронирования")
    @Test
    void throwExceptionWhenStateIsUnknownForOwner() {
        // ПОДГОТОВКА ДАННЫХ
        //
        String unknownState = "unsupported_state";

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(OWNER_ID)).thenReturn(Optional.of(owner));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.getOwnerBookings(OWNER_ID, unknownState))
                .isInstanceOf(IllegalBookingStateException.class)
                .hasMessage(String.format("Unknown state: %s", unknownState));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(OWNER_ID);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    private static Stream<Arguments> provideBookingStatesAndMockSetup() {
        return Stream.of(
                Arguments.of("ALL",
                        (BiConsumer<BookingRepository, List<Booking>>) (mock, res) -> Mockito.when(mock.findAllByItemOwnerIdOrderByStartDateDesc(OWNER_ID)).thenReturn(res),
                        (Consumer<BookingRepository>) mock -> Mockito.verify(mock, Mockito.times(1)).findAllByItemOwnerIdOrderByStartDateDesc(OWNER_ID)),
                Arguments.of("CURRENT",
                        (BiConsumer<BookingRepository, List<Booking>>) (mock, res) -> Mockito.when(mock.findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Mockito.eq(OWNER_ID), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(res),
                        (Consumer<BookingRepository>) mock -> Mockito.verify(mock, Mockito.times(1)).findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Mockito.eq(OWNER_ID), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))),
                Arguments.of("PAST",
                        (BiConsumer<BookingRepository, List<Booking>>) (mock, res) -> Mockito.when(mock.findAllByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(Mockito.eq(OWNER_ID), Mockito.any(LocalDateTime.class))).thenReturn(res),
                        (Consumer<BookingRepository>) mock -> Mockito.verify(mock, Mockito.times(1)).findAllByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(Mockito.eq(OWNER_ID), Mockito.any(LocalDateTime.class))),
                Arguments.of("FUTURE",
                        (BiConsumer<BookingRepository, List<Booking>>) (mock, res) -> Mockito.when(mock.findAllByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(Mockito.eq(OWNER_ID), Mockito.any(LocalDateTime.class))).thenReturn(res),
                        (Consumer<BookingRepository>) mock -> Mockito.verify(mock, Mockito.times(1)).findAllByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(Mockito.eq(OWNER_ID), Mockito.any(LocalDateTime.class))),
                Arguments.of("WAITING",
                        (BiConsumer<BookingRepository, List<Booking>>) (mock, res) -> Mockito.when(mock.findAllByItemOwnerIdAndStatusOrderByStartDateDesc(OWNER_ID, BookingStatus.WAITING)).thenReturn(res),
                        (Consumer<BookingRepository>) mock -> Mockito.verify(mock, Mockito.times(1)).findAllByItemOwnerIdAndStatusOrderByStartDateDesc(OWNER_ID, BookingStatus.WAITING)),
                Arguments.of("REJECTED",
                        (BiConsumer<BookingRepository, List<Booking>>) (mock, res) -> Mockito.when(mock.findAllByItemOwnerIdAndStatusOrderByStartDateDesc(OWNER_ID, BookingStatus.REJECTED)).thenReturn(res),
                        (Consumer<BookingRepository>) mock -> Mockito.verify(mock, Mockito.times(1)).findAllByItemOwnerIdAndStatusOrderByStartDateDesc(OWNER_ID, BookingStatus.REJECTED))
        );
    }
}