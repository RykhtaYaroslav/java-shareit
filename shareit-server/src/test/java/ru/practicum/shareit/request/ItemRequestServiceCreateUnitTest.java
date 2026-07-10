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
import ru.practicum.shareit.request.dto.ItemRequestCreateRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceCreateUnitTest {
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
    private final String description = "Нужна дрель для ремонта";

    private final User user = User.builder()
            .id(userId)
            .name("User")
            .email("user@mail.com")
            .build();

    @DisplayName("Успешное создание запроса на вещь")
    @Test
    void shouldCreateItemRequestSuccessfully() {
        // ПОДГОТОВКА ДАННЫХ
        //
        ItemRequestCreateRequestDto requestDto = new ItemRequestCreateRequestDto();
        requestDto.setDescription(description);

        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description(description)
                .user(user)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto expected = ItemRequestDto.builder()
                .id(requestId)
                .description(description)
                .created(itemRequest.getCreated())
                .items(Collections.emptyList())
                .build();

        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя, создающего запрос
        Mockito.when(userServiceMock.getUser(userId)).thenReturn(user);
        // Маппим входящий DTO в модель
        Mockito.when(itemRequestMapperMock.mapToModel(Mockito.eq(requestDto), Mockito.any(LocalDateTime.class), Mockito.eq(user)))
                .thenReturn(itemRequest);
        // Сохраняем модель в репозитории
        Mockito.when(itemRequestRepositoryMock.save(itemRequest)).thenReturn(itemRequest);
        // Маппим сохранённую модель в исходящий DTO с пустым списком вещей в ответе
        Mockito.when(itemRequestMapperMock.mapToDto(itemRequest, Collections.emptyList())).thenReturn(expected);

        // ВЫЗОВ МЕТОДА
        //
        ItemRequestDto result = itemRequestService.create(userId, requestDto);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Обратились в userServiceMock.getUser только один раз
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(userId);
        // Обратились в маппер для перевода в модель один раз
        Mockito.verify(itemRequestMapperMock, Mockito.times(1))
                .mapToModel(Mockito.eq(requestDto), Mockito.any(LocalDateTime.class), Mockito.eq(user));
        // Обратились в itemRequestRepositoryMock.save только один раз
        Mockito.verify(itemRequestRepositoryMock, Mockito.times(1)).save(itemRequest);
        // Обратились в маппер для перевода в итоговый DTO один раз
        Mockito.verify(itemRequestMapperMock, Mockito.times(1)).mapToDto(itemRequest, Collections.emptyList());
        // Больше не обращались в другие методы этих компонентов:
        Mockito.verifyNoMoreInteractions(userServiceMock, itemRequestMapperMock, itemRequestRepositoryMock);
        // В этот сервис не заходили вообще:
        Mockito.verifyNoInteractions(itemServiceMock);
    }

    @DisplayName("Ошибка NotFoundException, если создатель запроса отсутствует в системе")
    @Test
    void throwExceptionWhenUserNotFoundDuringRequestCreation() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongUserId = -1L;
        ItemRequestCreateRequestDto requestDto = new ItemRequestCreateRequestDto();
        requestDto.setDescription(description);

        // НАСТРОЙКА МОКОВ
        //
        // Сервис пользователей выбрасывает исключение, если пользователя нет
        Mockito.when(userServiceMock.getUser(wrongUserId))
                .thenThrow(new NotFoundException(String.format("Пользователь с id=%d не найден", wrongUserId)));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> itemRequestService.create(wrongUserId, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongUserId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userServiceMock.getUser только один раз
        Mockito.verify(userServiceMock, Mockito.times(1)).getUser(wrongUserId);
        // Больше не обращались в другие методы этого сервиса
        Mockito.verifyNoMoreInteractions(userServiceMock);
        // В эти компоненты не заходили вообще, так как выполнение прервалось на первой строчке
        Mockito.verifyNoInteractions(itemRequestRepositoryMock, itemRequestMapperMock, itemServiceMock);
    }
}