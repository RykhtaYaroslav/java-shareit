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
class ItemRequestServiceFindAllFromOthersUnitTest {
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
    private final Long otherRequestId = 2L;
    private final Long itemId = 10L;
    private final Integer from = 0;
    private final Integer size = 10;

    private final User user = User.builder()
            .id(userId)
            .name("CurrentUser")
            .email("current@mail.com")
            .build();

    private final ItemRequest otherItemRequest = ItemRequest.builder()
            .id(otherRequestId)
            .description("Нужен перфоратор")
            .user(User.builder().id(3L).name("OtherUser").build())
            .created(LocalDateTime.now())
            .build();

    private final Item itemResponse = Item.builder()
            .id(itemId)
            .ownerId(4L)
            .name("Перфоратор")
            .description("Мощный")
            .available(true)
            .itemRequestId(otherRequestId)
            .build();

    @DisplayName("Успешное получение списка чужих запросов с пагинацией и вещами-ответами")
    @Test
    void shouldReturnOtherUsersRequestsWithPaginationAndResponses() {
        // ПОДГОТОВКА ДАННЫХ
        //
        List<ItemRequest> requests = List.of(otherItemRequest);
        List<Long> requestIds = List.of(otherRequestId);
        List<Item> items = List.of(itemResponse);

        ItemRequestDto.ItemResponseDto itemResponseDto = ItemRequestDto.ItemResponseDto.builder()
                .id(itemId)
                .name("Перфоратор")
                .description("Мощный")
                .available(true)
                .build();

        ItemRequestDto expectedDto = ItemRequestDto.builder()
                .id(otherRequestId)
                .description("Нужен перфоратор")
                .created(otherItemRequest.getCreated())
                .items(List.of(itemResponseDto))
                .build();

        List<ItemRequestDto> expected = List.of(expectedDto);

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим текущего пользователя
        Mockito.when(userServiceMock.getUser(userId)).thenReturn(user);
        // Репозиторий возвращает страницу чужих запросов
        Mockito.when(itemRequestRepositoryMock.findAllByUserIdNotWithPagination(userId, from, size)).thenReturn(requests);
        // Выгружаем ответы для этих запросов
        Mockito.when(itemServiceMock.findAllByRequestsIds(requestIds)).thenReturn(items);
        // Маппер преобразует вещь в ItemResponseDto
        Mockito.when(itemRequestMapperMock.mapToItemResponseDto(itemResponse)).thenReturn(itemResponseDto);
        // Маппер собирает итоговый ItemRequestDto
        Mockito.when(itemRequestMapperMock.mapToDto(otherItemRequest, List.of(itemResponseDto))).thenReturn(expectedDto);

        // ВЫЗОВ МЕТОДА
        //
        List<ItemRequestDto> result = itemRequestService.findAllFromOthers(userId, from, size);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).hasSize(1).isEqualTo(expected);
        // Проверяем вызовы каждого компонента по одному разу
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(userId);
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findAllByUserIdNotWithPagination(userId, from, size);
        Mockito.verify(itemServiceMock, Mockito.times(1)).findAllByRequestsIds(requestIds);
        Mockito.verify(itemRequestMapperMock, Mockito.times(1)).mapToItemResponseDto(itemResponse);
        Mockito.verify(itemRequestMapperMock, Mockito.times(1)).mapToDto(otherItemRequest, List.of(itemResponseDto));
        // Больше не обращались к методам этих компонентов:
        Mockito.verifyNoMoreInteractions(userServiceMock, itemRequestRepositoryMock, itemServiceMock, itemRequestMapperMock);
    }

    @DisplayName("Успешное получение пустого списка, если чужих запросов в системе нет")
    @Test
    void shouldReturnEmptyListWhenNoOtherRequestsExist() {
        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userServiceMock.getUser(userId)).thenReturn(user);
        // Репозиторий возвращает пустую страницу
        Mockito.when(itemRequestRepositoryMock.findAllByUserIdNotWithPagination(userId, from, size)).thenReturn(Collections.emptyList());
        // Внутренние методы getResponses вызываются с пустой коллекцией идентификаторов
        Mockito.when(itemServiceMock.findAllByRequestsIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // ВЫЗОВ МЕТОДА
        //
        List<ItemRequestDto> result = itemRequestService.findAllFromOthers(userId, from, size);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEmpty();
        // Верифицируем вызовы компонентов
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(userId);
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).findAllByUserIdNotWithPagination(userId, from, size);
        Mockito.verify(itemServiceMock, Mockito.times(1)).findAllByRequestsIds(Collections.emptyList());
        // Больше не обращались к методам этих компонентов:
        Mockito.verifyNoMoreInteractions(userServiceMock, itemRequestRepositoryMock, itemServiceMock);
        Mockito.verifyNoInteractions(itemRequestMapperMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашивающий пользователь не существует")
    @Test
    void throwExceptionWhenUserNotFoundDuringFindAllFromOthers() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongUserId = -1L;

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(userServiceMock.getUser(wrongUserId))
                .thenThrow(new NotFoundException(String.format("Пользователь с id=%d не найден", wrongUserId)));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemRequestService.findAllFromOthers(wrongUserId, from, size))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongUserId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(wrongUserId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
        // В остальные компоненты не заходили, выполнение прервалось на первой строчке
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, itemServiceMock, itemRequestMapperMock);
    }
}