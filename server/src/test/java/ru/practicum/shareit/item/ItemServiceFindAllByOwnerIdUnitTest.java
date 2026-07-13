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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ItemServiceFindAllByOwnerIdUnitTest {
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

    private final Long itemId1 = 1L;
    private final Long itemId2 = 2L;
    private final Long ownerId = 1L;
    private final Long viewerId = 2L;

    private final User owner = User.builder().id(ownerId).name("Owner").email("owner@mail.com").build();
    private final User viewer = User.builder().id(viewerId).name("viewer").email("viewer@mail.com").build();

    private final Item item1 = new Item(itemId1, ownerId, "item1", "Description1", Boolean.TRUE, null);
    private final Item item2 = new Item(itemId2, ownerId, "item2", "Description2", Boolean.TRUE, null);

    @DisplayName("Успешное получение списка вещей владельца со всеми бронированиями и отзывами")
    @Test
    void shouldReturnItemsListWithBookingsAndComments() {
        // ПОДГОТОВКА ДАННЫХ
        //
        LocalDateTime time = LocalDateTime.now();

        Long pastBookingId = 1L;
        Long futureBookingId = 2L;
        boolean isPast = true;
        boolean isFuture = false;

        Booking bookingLast = createBooking(pastBookingId, item1, isPast, time);
        Booking bookingNext = createBooking(futureBookingId, item2, isFuture, time);

        Long commentId = 1L;

        Comment comment = createComment(commentId, item1);

        List<Long> itemIds = List.of(itemId1, itemId2);
        List<Item> items = List.of(item1, item2);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим владельца
        Mockito.when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(owner));
        // Успешно находим список вещей
        Mockito.when(itemRepositoryMock.findAllByOwnerIdOrderByIdAsc(ownerId)).thenReturn(items);
        // Возврат прошлого бронирования для первой вещи
        Mockito.when(bookingRepositoryMock.findPreviousBookingsForItems(Mockito.eq(itemIds), Mockito.any(LocalDateTime.class))).thenReturn(List.of(bookingLast));
        // Возврат будущего бронирования для второй вещи
        Mockito.when(bookingRepositoryMock.findFutureBookingsForItems(Mockito.eq(itemIds), Mockito.any(LocalDateTime.class))).thenReturn(List.of(bookingNext));
        // Возврат отзыва для первой вещи
        Mockito.when(commentRepositoryMock.findAllByItemIdInOrderByCreatedDesc(itemIds)).thenReturn(List.of(comment));

        // ВЫЗОВ МЕТОДА
        //
        List<ItemDto> result = itemService.findAllByOwnerId(ownerId);

        List<CommentDto> commentsDto = List.of(CommentDto.mapToDto(comment));
        ItemDto expectedItem1 = ItemDto.mapToDto(item1, bookingLast, null, commentsDto);
        ItemDto expectedItem2 = ItemDto.mapToDto(item2, null, bookingNext, Collections.emptyList());
        List<ItemDto> expected = List.of(expectedItem1, expectedItem2);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).hasSize(2).isEqualTo(expected);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(ownerId);
        // Обратились в itemRepositoryMock.findAllByOwnerIdOrderByIdAsc только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findAllByOwnerIdOrderByIdAsc(ownerId);
        // Обратились по разу в репозитории бронирований и отзывов
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findPreviousBookingsForItems(Mockito.eq(itemIds), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findFutureBookingsForItems(Mockito.eq(itemIds), Mockito.any(LocalDateTime.class));
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdInOrderByCreatedDesc(itemIds);
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, commentRepositoryMock, bookingRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(itemRequestRepositoryMock);
    }

    @DisplayName("Успешное получение пустого списка, если у владельца нет вещей")
    @Test
    void shouldReturnEmptyListWhenOwnerHasNoItems() {
        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим владельца
        Mockito.when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(owner));
        // Возвращаем пустой список вещей
        Mockito.when(itemRepositoryMock.findAllByOwnerIdOrderByIdAsc(ownerId)).thenReturn(Collections.emptyList());
        // Дополнительные приватные методы получают пустой список
        Mockito.when(bookingRepositoryMock.findPreviousBookingsForItems(Mockito.eq(Collections.emptyList()), Mockito.any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        Mockito.when(bookingRepositoryMock.findFutureBookingsForItems(Mockito.eq(Collections.emptyList()), Mockito.any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        Mockito.when(commentRepositoryMock.findAllByItemIdInOrderByCreatedDesc(Collections.emptyList())).thenReturn(Collections.emptyList());

        // ВЫЗОВ МЕТОДА
        //
        List<ItemDto> result = itemService.findAllByOwnerId(ownerId);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEmpty();
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(ownerId);
        // Обратились в itemRepositoryMock.findAllByOwnerIdOrderByIdAsc только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findAllByOwnerIdOrderByIdAsc(ownerId);
        // Проверяем вызовы с пустыми списками
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findPreviousBookingsForItems(Mockito.eq(Collections.emptyList()), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepositoryMock, Mockito.times(1)).findFutureBookingsForItems(Mockito.eq(Collections.emptyList()), Mockito.any(LocalDateTime.class));
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdInOrderByCreatedDesc(Collections.emptyList());
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, commentRepositoryMock, bookingRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(itemRequestRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если пользователь не существует")
    @Test
    void throwExceptionWhenUserDoesNotExist() {
        // НАСТРОЙКА МОКОВ
        //
        // НЕ находим пользователя в базе данных
        Mockito.when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemService.findAllByOwnerId(ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", ownerId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(ownerId);
        // Больше не обращались в другие методы этого репозитория:
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        // В эти репозитории не заходили вообще, так как выполнение прервалось на первой строчке:
        Mockito.verifyNoInteractions(itemRepositoryMock, itemRequestRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }

    private Comment createComment(Long commentId, Item itemForComment) {
        User user = new User(viewerId + commentId, "Name" + commentId, "email@email" + commentId);
        return new Comment(commentId, "text" + commentId, user, itemForComment, LocalDateTime.now());
    }

    private Booking createBooking(Long bookingId, Item item, boolean past, LocalDateTime time) {
        int dayOffset = 1;
        int hourOffset = 1;

        if (past) {
            return new Booking(bookingId, item, time.minusDays(dayOffset), time.minusHours(hourOffset), viewer, BookingStatus.APPROVED);
        }
        return new Booking(bookingId, item, time.plusHours(hourOffset), time.plusDays(dayOffset), viewer, BookingStatus.APPROVED);
    }
}