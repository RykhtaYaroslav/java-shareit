package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ItemServiceUpdateUnitTest {
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
    private final Long userId = 1L;

    private static final String OLD_NAME = "Old Name";
    private static final String NEW_NAME = "New Name";
    private static final String OLD_DESCRIPTION = "Old Description";
    private static final String NEW_DESCRIPTION = "New Description";
    private static final boolean OLD_AVAILABLE = true;
    private static final boolean NEW_AVAILABLE = false;

    private Item oldItem;

    private final User testUser = User.builder().id(userId).name("Owner").email("owner@mail.com").build();

    @BeforeEach
    void getOldItem() {
        oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setName(OLD_NAME);
        oldItem.setDescription(OLD_DESCRIPTION);
        oldItem.setOwnerId(userId);
        oldItem.setAvailable(OLD_AVAILABLE);
    }

    @DisplayName("Частичное успешное обновление полей (одного, двух, всех или ни одного) вещи владельцем")
    @ParameterizedTest
    @MethodSource("provideUpdateRequests")
    void partiallyUpdateItemFields(ItemDtoUpdateRequest request, String expectedName, String expectedDescription, Boolean expectedAvailable) {
        Mockito.when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(testUser));
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(oldItem));
        Mockito.when(itemRepositoryMock.save(oldItem)).thenReturn(oldItem);

        ItemDto newItemDto = itemService.update(userId, itemId, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedName, newItemDto.getName()),
                () -> Assertions.assertEquals(expectedDescription, newItemDto.getDescription()),
                () -> Assertions.assertEquals(expectedAvailable, newItemDto.getAvailable())
        );

        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(itemId);
        Mockito.verify(itemRepositoryMock, Mockito.times(1)).save(Mockito.any(Item.class));
        Mockito.verifyNoMoreInteractions(userRepositoryMock, itemRepositoryMock);
        Mockito.verifyNoInteractions(bookingRepositoryMock, commentRepositoryMock, itemRequestRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если вещь обновляет не владелец")
    @Test
    void throwNotFoundWhenUserIsNotOwner() {
        Long wrongUserId = 2L;
        User wrongUser = new User(wrongUserId, "Wrong", "Wrong@Wrong.wrong");

        Mockito.when(userRepositoryMock.findById(wrongUserId)).thenReturn(Optional.of(wrongUser));
        Mockito.when(itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(oldItem));

        ItemDtoUpdateRequest request = createUpdateRequest(OLD_NAME, OLD_DESCRIPTION, OLD_AVAILABLE);

        NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> itemService.update(wrongUserId, itemId, request));

        Assertions.assertEquals(
                String.format(
                        "У пользователя с id=%d нет доступа к редактированию предмета с id=%d", wrongUserId, itemId),
                e.getMessage());

        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongUserId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, commentRepositoryMock, bookingRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если обновляемая вещь отсутствует в базе данных")
    @Test
    void throwExceptionWhenItemNotFound() {
        Long wrongItemId = -1L;

        Mockito.when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(testUser));
        Mockito.when(itemRepositoryMock.findById(wrongItemId)).thenReturn(Optional.empty());

        ItemDtoUpdateRequest request = createUpdateRequest(OLD_NAME, OLD_DESCRIPTION, OLD_AVAILABLE);

        NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> itemService.update(userId, wrongItemId, request));

        Assertions.assertEquals(String.format("Предмет с id=%d не найден", wrongItemId), e.getMessage());

        Mockito.verify(itemRepositoryMock, Mockito.times(1)).findById(wrongItemId);
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        Mockito.verifyNoMoreInteractions(itemRepositoryMock, userRepositoryMock);
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если пользователь не существует")
    @Test
    void throwExceptionWhenUserDoesNotExist() {
        Long wrongUserId = -1L;

        Mockito.when(userRepositoryMock.findById(wrongUserId)).thenReturn(Optional.empty());

        ItemDtoUpdateRequest request = createUpdateRequest(OLD_NAME, OLD_DESCRIPTION, OLD_AVAILABLE);

        NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> itemService.update(wrongUserId, itemId, request));

        Assertions.assertEquals(String.format("Пользователь с id=%d не найден", wrongUserId), e.getMessage());

        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongUserId);
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
        Mockito.verifyNoInteractions(itemRepositoryMock, itemRequestRepositoryMock, bookingRepositoryMock, commentRepositoryMock);
    }

    private static Stream<Arguments> provideUpdateRequests() {
        return Stream.of(
                Arguments.of(createUpdateRequest(NEW_NAME, null, null), NEW_NAME, OLD_DESCRIPTION, OLD_AVAILABLE),
                Arguments.of(createUpdateRequest(null, NEW_DESCRIPTION, null), OLD_NAME, NEW_DESCRIPTION, OLD_AVAILABLE),
                Arguments.of(createUpdateRequest(null, null, NEW_AVAILABLE), OLD_NAME, OLD_DESCRIPTION, NEW_AVAILABLE),
                Arguments.of(createUpdateRequest(NEW_NAME, NEW_DESCRIPTION, null), NEW_NAME, NEW_DESCRIPTION, OLD_AVAILABLE),
                Arguments.of(createUpdateRequest(NEW_NAME, null, NEW_AVAILABLE), NEW_NAME, OLD_DESCRIPTION, NEW_AVAILABLE),
                Arguments.of(createUpdateRequest(null, NEW_DESCRIPTION, NEW_AVAILABLE), OLD_NAME, NEW_DESCRIPTION, NEW_AVAILABLE),
                Arguments.of(createUpdateRequest(null, null, null), OLD_NAME, OLD_DESCRIPTION, OLD_AVAILABLE)
        );
    }

    private static ItemDtoUpdateRequest createUpdateRequest(String name, String desc, Boolean available) {
        ItemDtoUpdateRequest req = new ItemDtoUpdateRequest();
        req.setName(name);
        req.setDescription(desc);
        req.setAvailable(available);
        return req;
    }
}
