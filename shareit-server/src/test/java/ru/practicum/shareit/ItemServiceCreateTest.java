package ru.practicum.shareit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ItemServiceCreateTest {

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
    private final Long itemRequestId = 1L;
    private final Long userId = 1L;

    private final User testUser = User.builder().id(userId).name("Owner").email("owner@mail.com").build();
    private final ItemDtoCreateRequest testRequest = new ItemDtoCreateRequest();

    @BeforeEach
    void getTestItemDtoCreateRequest() {
        testRequest.setAvailable(true);
        testRequest.setDescription("Description" + itemId);
        testRequest.setName("Name" + itemId);
    }

    @DisplayName("Успешное создание вещи без привязки к запросу")
    @Test
    void successfullyCreateItemWithoutRequest() {
        testRequest.setItemRequestId(null);

        Item savedItem = Item.builder()
                .id(itemId)
                .ownerId(userId)
                .name(testRequest.getName())
                .description(testRequest.getDescription())
                .available(testRequest.getAvailable())
                .itemRequestId(null)
                .build();

        ItemDto expectedItemDto = ItemDto.mapToDto(savedItem);

        Mockito.when(userRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));
        Mockito.when(itemRepositoryMock.save(Mockito.any(Item.class))).thenReturn(savedItem);

        ItemDto resultTestItemDto = itemService.create(userId, testRequest);
        Assertions.assertEquals(expectedItemDto, resultTestItemDto);

        // Проверка, что вызваны были только нужные репозитории и только нужное количество раз.
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).save(Mockito.any(Item.class));
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock);
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }

    @DisplayName("Успешное создание вещи с привязкой к существующему запросу")
    @Test
    void successfullyCreateItemWithRequest() {
        testRequest.setItemRequestId(itemRequestId);

        Item savedItem = Item.builder()
                .id(itemId)
                .ownerId(userId)
                .name(testRequest.getName())
                .description(testRequest.getDescription())
                .available(testRequest.getAvailable())
                .itemRequestId(testRequest.getItemRequestId())
                .build();

        ItemDto expectedItemDto = ItemDto.mapToDto(savedItem);

        Mockito.when(userRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));
        Mockito.when(itemRepositoryMock.save(Mockito.any(Item.class))).thenReturn(savedItem);
        Mockito.when(itemRequestRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(new ItemRequest()));

        ItemDto resultTestItemDto = itemService.create(userId, testRequest);
        Assertions.assertEquals(expectedItemDto, resultTestItemDto);

        // Проверка, что вызваны были только нужные репозитории и только нужное количество раз.
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).save(Mockito.any(Item.class));
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findById(itemRequestId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock, itemRequestRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock, commentRepositoryMock);

    }

    @DisplayName("Ошибка, если создатель вещи не существует в системе")
    @Test
    void throwExceptionWhenUserDoesNotExist() {
        Long wrongUserId = -1L;

        Mockito.when(userRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> itemService.create(wrongUserId, testRequest));

        Assertions.assertEquals(String.format("Пользователь с id=%d не найден", wrongUserId), e.getMessage());

        // Проверка, что вызваны были только нужные репозитории и только нужное количество раз.
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongUserId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, itemRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }

    @DisplayName("Ошибка, если указан несуществующий itemRequestId")
    @Test
    void throwExceptionWhenWrongItemRequestId() {
        Long wrongItemRequestId = -1L;

        testRequest.setItemRequestId(wrongItemRequestId);

        Mockito.when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(testUser));
        Mockito.when(itemRequestRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> itemService.create(userId, testRequest));

        Assertions.assertEquals("Request not found", e.getMessage());

        // Проверка, что вызваны были только нужные репозитории и только нужное количество раз.
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findById(wrongItemRequestId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRequestRepositoryMock);
        Mockito.verifyNoInteractions(itemRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }
}
