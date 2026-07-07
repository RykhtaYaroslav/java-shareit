package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceFindByIdUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepositoryMock;

    @Mock
    private ItemRequestMapper itemRequestMapperMock;

    @Mock
    private UserService userServiceMock;

    @Mock
    private ItemService itemServiceMock;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final Long itemId = 10L;

    private final User user = User.builder()
            .id(userId)
            .name("Requester")
            .email("requester@mail.com")
            .build();

    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(requestId)
            .description("Нужен сварочный аппарат")
            .user(user)
            .created(LocalDateTime.now())
            .build();

    private final Item itemResponse = Item.builder()
            .id(itemId)
            .ownerId(5L)
            .name("Сварочный аппарат")
            .description("Инверторный, 200 А")
            .available(true)
            .itemRequestId(requestId)
            .build();

    @DisplayName("Успешное получение запроса по ID со списком вещей-ответов")
    @Test
    void shouldReturnItemRequestByIdWithResponses() {
        // ПОДГОТОВКА ДАННЫХ
        //
        List<Long> requestIds = List.of(requestId);
        List<Item> items = List.of(itemResponse);

        ItemRequestDto.ItemResponseDto itemResponseDto = ItemRequestDto.ItemResponseDto.builder()
                .id(itemId)
                .name("Сварочный аппарат")
                .description("Инверторный, 200 А")
                .available(true)
                .build();

        ItemRequestDto expected = ItemRequestDto.builder()
                .id(requestId)
                .description("Нужен сварочный аппарат")
                .created(itemRequest.getCreated())
                .items(List.of(itemResponseDto))
                .build();

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя
        Mockito.when(userServiceMock.getUser(userId)).thenReturn(user);
        // Репозиторий успешно возвращает запрос по его ID
        Mockito.when(itemRequestRepositoryMock.findById(requestId)).thenReturn(Optional.of(itemRequest));
        // Выгружаем ответы для этого запроса
        Mockito.when(itemServiceMock.findAllByRequestsIds(requestIds)).thenReturn(items);
        // Маппер преобразует вещь в ItemResponseDto
        Mockito.when(itemRequestMapperMock.mapToItemResponseDto(itemResponse)).thenReturn(itemResponseDto);
        // Маппер собирает итоговый ItemRequestDto
        Mockito.when(itemRequestMapperMock.mapToDto(itemRequest, List.of(itemResponseDto))).thenReturn(expected);

        // ВЫЗОВ МЕТОДА
        //
        ItemRequestDto result = itemRequestService.findById(userId, requestId);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Проверяем вызовы каждого компонента по одному разу
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(userId);
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findById(requestId);
        Mockito.verify(itemServiceMock, Mockito.times(1)).findAllByRequestsIds(requestIds);
        Mockito.verify(itemRequestMapperMock, Mockito.times(1)).mapToItemResponseDto(itemResponse);
        Mockito.verify(itemRequestMapperMock, Mockito.times(1)).mapToDto(itemRequest, List.of(itemResponseDto));
        // Больше не обращались к методам этих компонентов:
        Mockito.verifyNoMoreInteractions(userServiceMock, itemRequestRepositoryMock, itemServiceMock, itemRequestMapperMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашиваемый запрос не существует")
    @Test
    void throwExceptionWhenItemRequestNotFoundById() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongRequestId = -1L;

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя
        Mockito.when(userServiceMock.getUser(userId)).thenReturn(user);
        // Запрос не найден в базе данных
        Mockito.when(itemRequestRepositoryMock.findById(wrongRequestId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemRequestService.findById(userId, wrongRequestId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Request not found");

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(userId);
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findById(wrongRequestId);
        // Больше не обращались к методам этих компонентов:
        Mockito.verifyNoMoreInteractions(userServiceMock, itemRequestRepositoryMock);
        // В эти компоненты не заходили
        Mockito.verifyNoInteractions(itemServiceMock, itemRequestMapperMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашивающий пользователь не существует")
    @Test
    void throwExceptionWhenUserNotFoundDuringFindRequestById() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongUserId = -1L;

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userServiceMock.getUser(wrongUserId))
                .thenThrow(new NotFoundException(String.format("Пользователь с id=%d не найден", wrongUserId)));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemRequestService.findById(wrongUserId, requestId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongUserId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(wrongUserId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
        // В остальные компоненты не заходили вообще
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, itemServiceMock, itemRequestMapperMock);
    }
}