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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ItemRequestFindAllByUserIdUnitTest {
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
            .description("Нужна стремянка")
            .user(user)
            .created(LocalDateTime.now())
            .build();

    private final Item itemResponse = Item.builder()
            .id(itemId)
            .ownerId(2L)
            .name("Стремянка")
            .description("Алюминиевая, 5 ступеней")
            .available(true)
            .itemRequestId(requestId)
            .build();

    @DisplayName("Успешное получение списка запросов пользователя с вещами-ответами")
    @Test
    void shouldReturnUserRequestsListWithResponses() {
        // ПОДГОТОВКА ДАННЫХ
        //
        List<ItemRequest> requests = List.of(itemRequest);
        List<Long> requestIds = List.of(requestId);
        List<Item> items = List.of(itemResponse);

        ItemRequestDto.ItemResponseDto itemResponseDto = ItemRequestDto.ItemResponseDto.builder()
                .id(itemId)
                .name("Стремянка")
                .description("Алюминиевая, 5 ступеней")
                .available(true)
                .build();

        ItemRequestDto expectedDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Нужна стремянка")
                .created(itemRequest.getCreated())
                .items(List.of(itemResponseDto))
                .build();

        List<ItemRequestDto> expected = List.of(expectedDto);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя
        Mockito.when(userServiceMock.getUser(userId)).thenReturn(user);
        // Репозиторий возвращает список запросов пользователя
        Mockito.when(itemRequestRepositoryMock.findAllByUserIdOrderByCreatedDesc(userId)).thenReturn(requests);
        // Выгружаем вещи для этих запросов
        Mockito.when(itemServiceMock.findAllByRequestsIds(requestIds)).thenReturn(items);
        // Маппер собирает ItemResponseDto для вещи
        Mockito.when(itemRequestMapperMock.mapToItemResponseDto(itemResponse)).thenReturn(itemResponseDto);
        // Маппер собирает итоговый ItemRequestDto
        Mockito.when(itemRequestMapperMock.mapToDto(itemRequest, List.of(itemResponseDto))).thenReturn(expectedDto);

        // ВЫЗОВ МЕТОДА
        //
        List<ItemRequestDto> result = itemRequestService.findAllByUserId(userId);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).hasSize(1).isEqualTo(expected);
        // Проверяем вызовы каждого компонента строго по одному разу
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(userId);
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findAllByUserIdOrderByCreatedDesc(userId);
        Mockito.verify(itemServiceMock, Mockito.times(1)).findAllByRequestsIds(requestIds);
        Mockito.verify(itemRequestMapperMock, Mockito.times(1)).mapToItemResponseDto(itemResponse);
        Mockito.verify(itemRequestMapperMock, Mockito.times(1)).mapToDto(itemRequest, List.of(itemResponseDto));
        // Больше не обращались к методам этих компонентов:
        Mockito.verifyNoMoreInteractions(userServiceMock, itemRequestRepositoryMock, itemServiceMock, itemRequestMapperMock);
    }

    @DisplayName("Успешное получение пустого списка, если у пользователя нет ни одного запроса")
    @Test
    void shouldReturnEmptyListWhenUserHasNoRequests() {
        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userServiceMock.getUser(userId)).thenReturn(user);
        // Репозиторий возвращает пустой список запросов
        Mockito.when(itemRequestRepositoryMock.findAllByUserIdOrderByCreatedDesc(userId)).thenReturn(Collections.emptyList());
        // Дополнительные приватные методы getResponses получают пустой список идентификаторов
        Mockito.when(itemServiceMock.findAllByRequestsIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // ВЫЗОВ МЕТОДА
        //
        List<ItemRequestDto> result = itemRequestService.findAllByUserId(userId);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEmpty();
        // Верифицируем вызовы компонентов
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(userId);
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findAllByUserIdOrderByCreatedDesc(userId);
        Mockito.verify(itemServiceMock, Mockito.times(1)).findAllByRequestsIds(Collections.emptyList());
        // Больше не обращались к методам этих компонентов
        Mockito.verifyNoMoreInteractions(userServiceMock, itemRequestRepositoryMock, itemServiceMock);
        Mockito.verifyNoInteractions(itemRequestMapperMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашивающий пользователь не существует")
    @Test
    void throwExceptionWhenUserNotFoundDuringFindAllRequests() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongUserId = -1L;

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userServiceMock.getUser(wrongUserId))
                .thenThrow(new NotFoundException(String.format("Пользователь с id=%d не найден", wrongUserId)));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemRequestService.findAllByUserId(wrongUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongUserId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(wrongUserId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
        // В остальные компоненты не заходили
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, itemServiceMock, itemRequestMapperMock);
    }
}