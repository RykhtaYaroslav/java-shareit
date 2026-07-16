package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ItemServiceFindByIdUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepositoryMock;

    @Mock
    private ItemRepository itemRepositoryMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private BookingRepository bookingRepositoryMock;

    @Mock
    private CommentRepository commentRepositoryMock;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final Long itemId = 1L;
    private final Long ownerId = 1L;
    private final Long viewerId = 2L;

    private final User owner = User.builder().id(ownerId).name("Owner").email("owner@mail.com").build();
    private final User viewer = User.builder().id(viewerId).name("Simple").email("Simple@mail.com").build();


    private final Item item = new Item(itemId, ownerId, "item", "Description", Boolean.TRUE, null);
    private final Item itemForComments = new Item(itemId, ownerId, "itemForComments", "Description", Boolean.TRUE, null);

    @DisplayName("Успешное нахождение вещи, когда пользователь не владелец, без отзывов")
    @Test
    void shouldFindItemByNotOwner() {
        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя, не хозяина
        Mockito.when(userRepositoryMock.findById(viewerId)).thenReturn(Optional.of(viewer));
        // Успешно находим item
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));
        // НЕ находим отзывы на Item
        Mockito.when(commentRepositoryMock.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());

        // ВЫЗОВ МЕТОДА
        //
        ItemDto result = itemService.findById(viewerId, itemId);
        ItemDto expected = ItemDto.mapToDto(item);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(expected).isEqualTo(result);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(viewerId);
        // Обратились в itemRepositoryMock.findById только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        // Обратились в commentRepository.findAllByItemIdOrderByCreatedDesc только один раз
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdOrderByCreatedDesc(itemId);
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, commentRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(bookingRepositoryMock, itemRequestRepositoryMock);
    }

    @DisplayName("Успешное нахождение вещи НЕ владельцем с отзывами")
    @Test
    void shouldReturnItemDtoWithComments() {
        Comment comment1 = createComment(1L);
        Comment comment2 = createComment(2L);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя, не хозяина
        Mockito.when(userRepositoryMock.findById(viewerId)).thenReturn(Optional.of(viewer));
        // Успешно находим item
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(itemForComments));
        // Находим лист отзывов
        Mockito.when(commentRepositoryMock.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(List.of(comment1, comment2));

        // ВЫЗОВ МЕТОДА
        //
        ItemDto result = itemService.findById(viewerId, itemId);

        List<CommentDto> commentsDto = Stream.of(comment1, comment2).map(CommentDto::mapToDto).toList();
        ItemDto expected = ItemDto.mapToDto(itemForComments, commentsDto);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(expected).isEqualTo(result);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(viewerId);
        // Обратились в itemRepositoryMock.findById только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        // Обратились в commentRepository.findAllByItemIdOrderByCreatedDesc только один раз
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdOrderByCreatedDesc(itemId);
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, commentRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(bookingRepositoryMock, itemRequestRepositoryMock);
    }

    @DisplayName("Успешный запрос вещи владельцем с прошлым и будущим бронированиями")
    @Test
    void shouldReturnItemToOwnerWithBookingsInPastAndFuture() {
        // ПОДГОТОВКА ДАННЫХ
        //
        LocalDateTime time = LocalDateTime.now();

        Long pastBookingId = 1L;
        Long futureBookingId = 2L;
        boolean isPast = true;
        boolean isFuture = false;

        Booking bookingLast = createBooking(pastBookingId, isPast, time);
        Booking bookingNext = createBooking(futureBookingId, isFuture, time);

        List<Long> itemsId = List.of(itemId);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим владельца
        Mockito.when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(owner));
        // Успешно находим item
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));
        // НЕ находим отзывы на Item. Тест на отзывы есть в другом месте, он одинаково работает для владельца или не владельца. Нет смысла тестировать дополнительно
        Mockito.when(commentRepositoryMock.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());
        // Возврат прошлого бронирования
        Mockito.when(bookingRepositoryMock.findPreviousBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class))).thenReturn(List.of(bookingLast));
        // Возврат будущего бронирования
        Mockito.when(bookingRepositoryMock.findFutureBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class))).thenReturn(List.of(bookingNext));

        // ВЫЗОВ МЕТОДА
        //
        ItemDto result = itemService.findById(ownerId, itemId);

        ItemDto expected = ItemDto.mapToDto(item, bookingLast, bookingNext, Collections.emptyList());

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(expected).isEqualTo(result);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(ownerId);
        // Обратились в itemRepositoryMock.findById только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        // Обратились в commentRepository.findAllByItemIdOrderByCreatedDesc только один раз
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdOrderByCreatedDesc(itemId);
        // Обратились по разу в репозиторий букингов
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findPreviousBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findFutureBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class));
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, commentRepositoryMock, bookingRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(itemRequestRepositoryMock);
    }

    @DisplayName("Успешный запрос от владельца, без бронирований")
    @Test
    void shouldReturnItemWithoutBookings() {
        List<Long> itemsId = List.of(itemId);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим владельца
        Mockito.when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(owner));
        // Успешно находим item
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));
        // НЕ находим отзывы на Item. Тест на отзывы есть в другом месте, он одинаково работает для владельца или не владельца. Нет смысла тестировать дополнительно
        Mockito.when(commentRepositoryMock.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());
        // Возврат прошлого бронирования
        Mockito.when(bookingRepositoryMock.findPreviousBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        // Возврат будущего бронирования
        Mockito.when(bookingRepositoryMock.findFutureBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // ВЫЗОВ МЕТОДА
        //
        ItemDto result = itemService.findById(ownerId, itemId);

        ItemDto expected = ItemDto.mapToDto(item, null, null, Collections.emptyList());

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(expected).isEqualTo(result);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(ownerId);
        // Обратились в itemRepositoryMock.findById только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        // Обратились в commentRepository.findAllByItemIdOrderByCreatedDesc только один раз
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdOrderByCreatedDesc(itemId);
        // Обратились по разу в репозиторий букингов
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findPreviousBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findFutureBookingsForItems(Mockito.eq(itemsId), Mockito.any(LocalDateTime.class));
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, commentRepositoryMock, bookingRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(itemRequestRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если вещь отсутствует в базе")
    @Test
    void throwExceptionWhenItemNotFound() {
        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя
        Mockito.when(userRepositoryMock.findById(viewerId)).thenReturn(Optional.of(viewer));
        // НЕ находим item в базе данных
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemService.findById(viewerId, itemId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Предмет с id=%d не найден", itemId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(viewerId);
        // Обратились в itemRepositoryMock.findById только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если пользователь не существует")
    @Test
    void throwExceptionWhenUserDoesNotExist() {
        // НАСТРОЙКА МОКОВ
        //
        // НЕ находим пользователя в базе данных
        Mockito.when(userRepositoryMock.findById(viewerId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemService.findById(viewerId, itemId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", viewerId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(viewerId);
        // Больше не обращались в другие методы этого репозитория:
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        // В эти репозитории не заходили вообще, так как выполнение прервалось на первой строчке:
        Mockito.verifyNoInteractions(itemRepositoryMock, itemRequestRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }


    private Comment createComment(Long commentId) {
        User user = new User(viewerId + commentId, "Name" + commentId, "email@email" + commentId);
        return new Comment(commentId, "text" + commentId, user, itemForComments, LocalDateTime.now());
    }

    private Booking createBooking(Long bookingId, boolean past, LocalDateTime time) {
        int dayOffset = 1;
        int hourOffset = 1;

        if (past) {
            return new Booking(bookingId, item, time.minusDays(dayOffset), time.minusHours(hourOffset), viewer, BookingStatus.APPROVED);
        }
        return new Booking(bookingId, item, time.plusHours(hourOffset), time.plusDays(dayOffset), viewer, BookingStatus.APPROVED);
    }
}
