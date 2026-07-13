package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserServiceFindByIdAndFindAllUnitTest {
    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private UserServiceImpl userService;

    private final Long userId1 = 1L;
    private final Long userId2 = 2L;

    private final User user1 = User.builder().id(userId1).name("User One").email("one@mail.com").build();
    private final User user2 = User.builder().id(userId2).name("User Two").email("two@mail.com").build();

    @DisplayName("Успешное нахождение пользователя по его ID")
    @Test
    void shouldFindUserByIdSuccessfully() {
        // НАСТРОЙКА МОКОВ
        //
        // Успешно находим пользователя в базе данных
        Mockito.when(userRepositoryMock.findById(userId1)).thenReturn(Optional.of(user1));

        // ВЫЗОВ МЕТОДА
        //
        UserDto result = userService.findById(userId1);
        UserDto expected = UserDto.mapToDto(user1);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId1);
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если запрашиваемый пользователь отсутствует в базе данных")
    @Test
    void throwExceptionWhenUserNotFoundById() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongUserId = -1L;

        // НАСТРОЙКА МОКОВ
        //
        // Пользователь не найден в базе данных
        Mockito.when(userRepositoryMock.findById(wrongUserId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> userService.findById(wrongUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongUserId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongUserId);
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }

    @DisplayName("Успешное получение списка всех пользователей")
    @Test
    void shouldReturnAllUsersList() {
        // ПОДГОТОВКА ДАННЫХ
        //
        List<User> usersList = List.of(user1, user2);

        // НАСТРОЙКА МОКОВ
        //
        // Репозиторий возвращает список из двух пользователей
        Mockito.when(userRepositoryMock.findAll()).thenReturn(usersList);

        // ВЫЗОВ МЕТОДА
        //
        List<UserDto> result = userService.findAll();
        List<UserDto> expected = List.of(UserDto.mapToDto(user1), UserDto.mapToDto(user2));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).hasSize(2).isEqualTo(expected);
        // Обратились в userRepositoryMock.findAll только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findAll();
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }

    @DisplayName("Успешное получение пустого списка, если в базе нет ни одного пользователя")
    @Test
    void shouldReturnEmptyListWhenNoUsersInDatabase() {
        // НАСТРОЙКА МОКОВ
        //
        // Репозиторий возвращает пустой список
        Mockito.when(userRepositoryMock.findAll()).thenReturn(Collections.emptyList());

        // ВЫЗОВ МЕТОДА
        //
        List<UserDto> result = userService.findAll();

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEmpty();
        // Обратились в userRepositoryMock.findAll только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findAll();
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }
}