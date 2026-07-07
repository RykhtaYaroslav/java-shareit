package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BookingServiceCreateUnitTest {
    @Mock
    private BookingRepository bookingRepositoryMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private ItemRepository itemRepositoryMock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final Long bookerId = 1L;
    private final Long ownerId = 2L;
    private final Long itemId = 1L;
    private final Long bookingId = 1L;

    private final User booker = User.builder().id(bookerId).name("Booker").email("booker@mail.com").build();
    private final User owner = User.builder().id(ownerId).name("Owner").email("owner@mail.com").build();
    private final Item item = new Item(itemId, ownerId, "Дрель", "Простая дрель", true, null);

    @DisplayName("Успешное создание бронирования")
    @Test
    void shouldCreateBookingSuccessfully() {
        // ПОДГОТОВКА ДАННЫХ
        //
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        Booking savedBooking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .startDate(start)
                .endDate(end)
                .status(BookingStatus.WAITING)
                .build();

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим автора бронирования
        Mockito.when(userRepositoryMock.findById(bookerId)).thenReturn(Optional.of(booker));
        // Успешно находим бронируемую вещь
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));
        // Пересечений по датам в базе нет — возвращаем пустой список
        Mockito.when(bookingRepositoryMock.findBookingInPeriod(Mockito.eq(itemId), Mockito.eq(start), Mockito.eq(end), Mockito.anyList()))
                .thenReturn(Collections.emptyList());
        // Успешно сохраняем бронирование в базу данных
        Mockito.when(bookingRepositoryMock.save(Mockito.any(Booking.class))).thenReturn(savedBooking);

        // ВЫЗОВ МЕТОДА
        //
        BookingDto result = bookingService.create(bookerId, requestDto);
        BookingDto expected = BookingDto.mapToDto(savedBooking);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Проверяем вызовы каждого репозитория по одному разу
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(bookerId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findBookingInPeriod(Mockito.eq(itemId), Mockito.eq(start), Mockito.eq(end), Mockito.anyList());
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).save(Mockito.any(Booking.class));
        // Больше не обращались к методам этих репозиториев
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если автор бронирования не найден")
    @Test
    void throwExceptionWhenBookerNotFound() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongBookerId = -1L;
        BookingRequestDto requestDto = BookingRequestDto.builder().itemId(itemId).build();

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(wrongBookerId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.create(wrongBookerId, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongBookerId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongBookerId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(itemRepositoryMock, bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если вещь не найдена")
    @Test
    void throwExceptionWhenItemNotFound() {
        // ПОДГОТОВКА ДАННЫХ
        //
        BookingRequestDto requestDto = BookingRequestDto.builder().itemId(itemId).build();

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(bookerId)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.create(bookerId, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Предмет с id=%d не найден", itemId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(bookerId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если владелец пытается забронировать собственную вещь")
    @Test
    void throwExceptionWhenOwnerTriesToBookHisOwnItem() {
        // ПОДГОТОВКА ДАННЫХ
        //
        BookingRequestDto requestDto = BookingRequestDto.builder().itemId(itemId).build();

        // НАСТРОЙКА МОКОВ
        //
        // Передаем ownerId в качестве создателя бронирования
        Mockito.when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.create(ownerId, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d является владельцем предмета с id=%d и не может его забронировать", ownerId, itemId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(ownerId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotAvailableException, если вещь недоступна для бронирования")
    @Test
    void throwExceptionWhenItemIsNotAvailable() {
        // ПОДГОТОВКА ДАННЫХ
        //
        BookingRequestDto requestDto = BookingRequestDto.builder().itemId(itemId).build();
        Item unavailableItem = new Item(itemId, ownerId, "Дрель", "Сломанная", false, null);

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(bookerId)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(unavailableItem));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.create(bookerId, requestDto))
                .isInstanceOf(NotAvailableException.class)
                .hasMessage("Предмет не доступен");

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(bookerId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotAvailableException, если дата начала бронирования позже даты окончания")
    @Test
    void throwExceptionWhenStartIsAfterEnd() {
        // ПОДГОТОВКА ДАННЫХ
        //
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(2); // Конец раньше начала

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(bookerId)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.create(bookerId, requestDto))
                .isInstanceOf(NotAvailableException.class)
                .hasMessage("Выбран некорректный срок аренды");

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(bookerId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotAvailableException, если даты пересекаются с существующим бронированием")
    @Test
    void throwExceptionWhenDatesIntersectWithExistingBooking() {
        // ПОДГОТОВКА ДАННЫХ
        //
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        Booking intersectingBooking = Booking.builder().id(999L).build();

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userRepositoryMock.findById(bookerId)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));
        // Репозиторий находит пересекающееся бронирование на эти даты
        Mockito.when(bookingRepositoryMock.findBookingInPeriod(Mockito.eq(itemId), Mockito.eq(start), Mockito.eq(end), Mockito.anyList()))
                .thenReturn(List.of(intersectingBooking));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> bookingService.create(bookerId, requestDto))
                .isInstanceOf(NotAvailableException.class)
                .hasMessage("На выбранный период времени предмет уже забронирован");

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(bookerId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findBookingInPeriod(Mockito.eq(itemId), Mockito.eq(start), Mockito.eq(end), Mockito.anyList());
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, bookingRepositoryMock);
    }
}