package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoCreateRequest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {
    private final ItemService itemService;

    private final UserService userService;
    private final CommentService commentService;

    private static final String USER_NAME = "User Name";

    private static final String TEST_ITEM_NAME = "Test Item Name";
    private static final String TEST_ITEM_DESCRIPTION = "Test Item Description";
    private static final Boolean TEST_ITEM_AVAILABLE = Boolean.TRUE;

    private static final String NEW_TEST_ITEM_NAME = "New Test Item Name";
    private static final String NEW_TEST_ITEM_DESCRIPTION = "New Test Item Description";
    private static final Boolean NEW_TEST_ITEM_AVAILABLE = Boolean.FALSE;

    @Autowired
    private BookingService bookingService;

    @DisplayName("Успешное создание нового item")
    @Test
    void shouldCreateItem() {
        UserDto anyUser = createUser(USER_NAME);

        ItemDtoCreateRequest request = getItemDtoCreateRequest();

        ItemDto result = itemService.create(anyUser.getId(), request);

        assertThat(result.getRequestId()).isNull();
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getAvailable()).isEqualTo(request.getAvailable());
        assertThat(result.getDescription()).isEqualTo(request.getDescription());
    }

    @DisplayName("Успешное обновление полей item")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUpdatesData")
    void shouldUpdateItemFields(String nameOfTest, ItemDtoUpdateRequest request, String expectedName, String expectedDescription, Boolean expectedAvailable) {
        UserDto user = createUser(USER_NAME);

        ItemDto oldItemDto = createNewItemDto(user);

        ItemDto newItemDto = itemService.update(user.getId(), oldItemDto.getId(), request);

        assertThat(newItemDto.getName()).isEqualTo(expectedName);
        assertThat(newItemDto.getDescription()).isEqualTo(expectedDescription);
        assertThat(newItemDto.getAvailable()).isEqualTo(expectedAvailable);

    }

    @DisplayName("Успешный поиск по id item с подгрузкой отзывов и бронирований")
    @Test
    void shouldReturnItemByIdForOwnerWithCommentsAndBookings() throws InterruptedException {
        UserDto owner = createUser(USER_NAME);
        Long ownerId = owner.getId();

        ItemDto itemDto = createNewItemDto(owner);
        Long itemId = itemDto.getId();

        UserDto commenter = createUser("Other Name");
        Long commenterId = commenter.getId();

        // Create comment and booking
        // Тут я использовал костыль в виде ожидания. Но потом разобрался, что можно было просто сохранить данные с помощью
        // EntityManager, которым я пользуюсь в тестах Booking. Если разрешите не переделывать, то я бы оставил как есть))
        BookingDto bookingDto = createBooking(commenterId, itemId);
        bookingDto = approve(ownerId, bookingDto.getId());

        TimeUnit.MILLISECONDS.sleep(15);

        CommentDto commentDto = createComment(commenterId, itemId);
        // ^Create comment and booking

        ItemDto result = itemService.findById(owner.getId(), itemDto.getId());

        assertThat(result.getId()).isEqualTo(itemDto.getId());
        assertThat(result.getComments()).hasSize(1).containsExactly(commentDto);
        assertThat(result.getLastBooking().getId()).isEqualTo(bookingDto.getId());
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getName()).isEqualTo(TEST_ITEM_NAME);
        assertThat(result.getDescription()).isEqualTo(TEST_ITEM_DESCRIPTION);
    }

    @DisplayName("Успешный поиск по строке")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSearchData")
    void shouldReturnCorrectListOfItems(String nameOfTest, String text, int expectedSize) {
        UserDto owner = createUser("searchOwner");

        // Создаем стандартную доступную вещь
        ItemDto availableItem = createNewItemDto(owner);

        // Создаем недоступную вещь
        ItemDtoCreateRequest unavailableRequest = getItemDtoCreateRequest();
        unavailableRequest.setAvailable(false);
        itemService.create(owner.getId(), unavailableRequest);

        List<ItemDto> result = itemService.searchByNameOrDescription(text);

        assertThat(result).hasSize(expectedSize);

        if (expectedSize > 0) {
            assertThat(result.getFirst().getId()).isEqualTo(availableItem.getId());
        } else {
            assertThat(result).isEmpty();
        }
    }

    private static Stream<Arguments> getSearchData() {
        return Stream.of(
                Arguments.of("Поиск по названию", "item", 1),
                Arguments.of("Поиск по описанию", "description", 1),
                Arguments.of("Запрос не совпадает ни с чем", "abracadabra", 0)
        );
    }

    private static Stream<Arguments> getUpdatesData() {
        return Stream.of(
                // 1. Одиночные обновления (по одному полю)
                Arguments.of("Обновление name", getUpdateRequest(NEW_TEST_ITEM_NAME, null, null), NEW_TEST_ITEM_NAME, TEST_ITEM_DESCRIPTION, TEST_ITEM_AVAILABLE),
                Arguments.of("Обновление description", getUpdateRequest(null, NEW_TEST_ITEM_DESCRIPTION, null), TEST_ITEM_NAME, NEW_TEST_ITEM_DESCRIPTION, TEST_ITEM_AVAILABLE),
                Arguments.of("Обновление available", getUpdateRequest(null, null, NEW_TEST_ITEM_AVAILABLE), TEST_ITEM_NAME, TEST_ITEM_DESCRIPTION, NEW_TEST_ITEM_AVAILABLE),

                // 2. Парные обновления (по два поля)
                Arguments.of("Обновление name и description", getUpdateRequest(NEW_TEST_ITEM_NAME, NEW_TEST_ITEM_DESCRIPTION, null), NEW_TEST_ITEM_NAME, NEW_TEST_ITEM_DESCRIPTION, TEST_ITEM_AVAILABLE),
                Arguments.of("Обновление name и available", getUpdateRequest(NEW_TEST_ITEM_NAME, null, NEW_TEST_ITEM_AVAILABLE), NEW_TEST_ITEM_NAME, TEST_ITEM_DESCRIPTION, NEW_TEST_ITEM_AVAILABLE),
                Arguments.of("Обновление description и available", getUpdateRequest(null, NEW_TEST_ITEM_DESCRIPTION, NEW_TEST_ITEM_AVAILABLE), TEST_ITEM_NAME, NEW_TEST_ITEM_DESCRIPTION, NEW_TEST_ITEM_AVAILABLE),

                // 3. Все поля и ни одного поля
                Arguments.of("Обновление всех полей", getUpdateRequest(NEW_TEST_ITEM_NAME, NEW_TEST_ITEM_DESCRIPTION, NEW_TEST_ITEM_AVAILABLE), NEW_TEST_ITEM_NAME, NEW_TEST_ITEM_DESCRIPTION, NEW_TEST_ITEM_AVAILABLE),
                Arguments.of("Ни одно поле не обновляется", getUpdateRequest(null, null, null), TEST_ITEM_NAME, TEST_ITEM_DESCRIPTION, TEST_ITEM_AVAILABLE)
        );
    }

    private UserDto createUser(String name) {
        UserDtoCreateRequest userDtoCreateRequest = UserDtoCreateRequest
                .builder()
                .name("User")
                .email(name + "@email.email")
                .build();
        return userService.create(userDtoCreateRequest);
    }

    private static ItemDtoUpdateRequest getUpdateRequest(String name, String description, Boolean available) {
        ItemDtoUpdateRequest request = new ItemDtoUpdateRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAvailable(available);
        return request;
    }

    private ItemDtoCreateRequest getItemDtoCreateRequest() {
        ItemDtoCreateRequest itemDtoCreateRequest = new ItemDtoCreateRequest();
        itemDtoCreateRequest.setName(TEST_ITEM_NAME);
        itemDtoCreateRequest.setAvailable(TEST_ITEM_AVAILABLE);
        itemDtoCreateRequest.setDescription(TEST_ITEM_DESCRIPTION);
        return itemDtoCreateRequest;
    }

    private ItemDto createNewItemDto(UserDto user) {
        ItemDtoCreateRequest itemDtoCreateRequest = getItemDtoCreateRequest();

        return itemService.create(user.getId(), itemDtoCreateRequest);
    }

    private CommentDto createComment(Long userId, Long itemId) {
        CommentDtoCreateRequest createRequest = CommentDtoCreateRequest.builder().text("text of comment").build();

        return commentService.createComment(userId, itemId, createRequest);
    }

    private BookingDto createBooking(Long userId, Long itemId) {
        int oneMillisecondInNanoseconds = 1000000;
        LocalDateTime start = LocalDateTime.now().plusNanos(oneMillisecondInNanoseconds * 5);
        LocalDateTime end = start.plusNanos(oneMillisecondInNanoseconds * 10);
        BookingDtoCreateRequest createRequest = BookingDtoCreateRequest
                .builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
        return bookingService.create(userId, createRequest);
    }

    private BookingDto approve(Long userId, Long bookingId) {
        return bookingService.approve(userId, bookingId, true);
    }
}
