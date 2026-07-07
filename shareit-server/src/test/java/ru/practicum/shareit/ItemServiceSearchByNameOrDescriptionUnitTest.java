package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ItemServiceSearchByNameOrDescriptionUnitTest {
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

    private final Item availableItem = new Item(itemId1, ownerId, "Дрель Аккумуляторная", "Очень мощная дрель", Boolean.TRUE, null);
    private final Item unavailableItem = new Item(itemId2, ownerId, "Старая Дрель", "Сломанный инструмент", Boolean.FALSE, null);

    @DisplayName("Успешный поиск вещей по названию или описанию с подгрузкой отзывов и фильтрацией недоступных")
    @Test
    void shouldReturnAvailableItemsWithCommentsBySearchText() {
        // ПОДГОТОВКА ДАННЫХ
        //
        String searchText = "дРеЛь";

        List<Long> allFoundItemIds = List.of(itemId1, itemId2);
        List<Item> repositoryResult = List.of(availableItem, unavailableItem);

        Long commentId = 1L;
        Comment comment = createComment(commentId, availableItem);

        // НАСТРОЙКА МОКОВ
        //
        // Репозиторий возвращает все совпадения, включая недоступные для аренды вещи
        Mockito.when(itemRepositoryMock.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchText, searchText))
                .thenReturn(repositoryResult);
        // Ищем отзывы
        Mockito.when(commentRepositoryMock.findAllByItemIdInOrderByCreatedDesc(allFoundItemIds))
                .thenReturn(List.of(comment));

        // ВЫЗОВ МЕТОДА
        //
        List<ItemDto> result = itemService.searchByNameOrDescription(searchText);

        List<CommentDto> commentsDto = List.of(CommentDto.mapToDto(comment));
        ItemDto expectedItemDto = ItemDto.mapToDto(availableItem, commentsDto);
        List<ItemDto> expected = List.of(expectedItemDto);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // в финальный результат попала только доступная вещь
        assertThat(result).hasSize(1).isEqualTo(expected);

        // Обратились в itemRepositoryMock только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1))
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchText, searchText);
        //
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdInOrderByCreatedDesc(allFoundItemIds);

        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(itemRepositoryMock, commentRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(userRepositoryMock, bookingRepositoryMock, itemRequestRepositoryMock);
    }

    @DisplayName("Возврат пустого списка, если строка поиска пустая или состоит только из пробелов")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void shouldReturnEmptyListWhenSearchTextIsBlank(String blankSearchText) {
        // ВЫЗОВ МЕТОДА
        //
        List<ItemDto> result = itemService.searchByNameOrDescription(blankSearchText);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEmpty();
        // Ни один репозиторий не должен вызываться, метод завершается на первой строчке
        Mockito.verifyNoInteractions(userRepositoryMock, itemRepositoryMock, commentRepositoryMock, bookingRepositoryMock, itemRequestRepositoryMock);
    }

    @DisplayName("Возврат пустого списка, если по ключевому слову ничего не найдено")
    @Test
    void shouldReturnEmptyListWhenNothingFoundInRepository() {
        // ПОДГОТОВКА ДАННЫХ
        //
        String textNotFound = "укулеле";

        // НАСТРОЙКА МОКОВ
        //
        Mockito.when(itemRepositoryMock.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(textNotFound, textNotFound))
                .thenReturn(Collections.emptyList());
        // метод получения отзывов вызывается с пустым списком
        Mockito.when(commentRepositoryMock.findAllByItemIdInOrderByCreatedDesc(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // ВЫЗОВ МЕТОДА
        //
        List<ItemDto> result = itemService.searchByNameOrDescription(textNotFound);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEmpty();
        // Обратились в itemRepositoryMock только один раз
        Mockito.verify(itemRepositoryMock, Mockito.times(1))
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(textNotFound, textNotFound);
        // Обратились в commentRepositoryMock с пустым списком
        Mockito.verify(commentRepositoryMock, Mockito.times(1)).findAllByItemIdInOrderByCreatedDesc(Collections.emptyList());
        // Больше не обращались в другие методы этих репозиториев:
        Mockito.verifyNoMoreInteractions(itemRepositoryMock, commentRepositoryMock);
        // В эти репозитории не заходили вообще:
        Mockito.verifyNoInteractions(userRepositoryMock, bookingRepositoryMock, itemRequestRepositoryMock);
    }

    private Comment createComment(Long commentId, Item itemForComment) {
        User user = new User(viewerId + commentId, "Name" + commentId, "email@email" + commentId);
        return new Comment(commentId, "text" + commentId, user, itemForComment, LocalDateTime.now());
    }
}