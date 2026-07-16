package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataIntegrityConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserServiceUpdateUnitTest {
    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private UserServiceImpl userService;

    private final Long userId = 1L;
    private final String oldName = "Old Name";
    private final String oldEmail = "old@mail.com";
    private final String newName = "New Name";
    private final String newEmail = "new@mail.com";

    private final User oldUser = User.builder()
            .id(userId)
            .name(oldName)
            .email(oldEmail)
            .build();

    @DisplayName("Успешное частичное обновление только имени пользователя (email равен null)")
    @Test
    void shouldUpdateOnlyUserName() {
        // ПОДГОТОВКА ДАННЫХ
        //
        UserDtoUpdateRequest updateRequest = new UserDtoUpdateRequest();
        updateRequest.setName(newName);
        updateRequest.setEmail(null);

        User updatedUser = User.builder()
                .id(userId)
                .name(newName)
                .email(oldEmail)
                .build();

        // НАСТРОЙКА МОКОВ
        //
        // Успешно вытаскиваем старого пользователя из базы
        Mockito.when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(oldUser));
        // Успешно сохраняем обновлённого пользователя
        Mockito.when(userRepositoryMock.save(Mockito.any(User.class))).thenReturn(updatedUser);

        // ВЫЗОВ МЕТОДА
        //
        UserDto result = userService.update(userId, updateRequest);
        UserDto expected = UserDto.mapToDto(updatedUser);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        // Обратились в userRepositoryMock.save только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(Mockito.any(User.class));
        // Больше не обращались в другие методы этого репозитория (включая findByEmail, так как email был null)
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }

    @DisplayName("Успешное частичное обновление только email пользователя (имя равно null)")
    @Test
    void shouldUpdateOnlyUserEmail() {
        // ПОДГОТОВКА ДАННЫХ
        //
        UserDtoUpdateRequest updateRequest = new UserDtoUpdateRequest();
        updateRequest.setName(null);
        updateRequest.setEmail(newEmail);

        User updatedUser = User.builder()
                .id(userId)
                .name(oldName)
                .email(newEmail)
                .build();

        // НАСТРОЙКА МОКОВ
        //
        // Успешно вытаскиваем старого пользователя из базы
        Mockito.when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(oldUser));
        // Новый email свободен
        Mockito.when(userRepositoryMock.findByEmail(newEmail)).thenReturn(Optional.empty());
        // Успешно сохраняем обновлённого пользователя
        Mockito.when(userRepositoryMock.save(Mockito.any(User.class))).thenReturn(updatedUser);

        // ВЫЗОВ МЕТОДА
        //
        UserDto result = userService.update(userId, updateRequest);
        UserDto expected = UserDto.mapToDto(updatedUser);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        // Проверили уникальность нового email
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findByEmail(newEmail);
        // Обратились в userRepositoryMock.save только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(Mockito.any(User.class));
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }

    @DisplayName("Ошибка DataIntegrityConflictException, если новый email уже занят другим пользователем")
    @Test
    void throwExceptionWhenUpdatingEmailIsAlreadyTaken() {
        // ПОДГОТОВКА ДАННЫХ
        //
        UserDtoUpdateRequest updateRequest = new UserDtoUpdateRequest();
        updateRequest.setEmail(newEmail);

        User anotherUser = User.builder()
                .id(99L)
                .name("Another User")
                .email(newEmail)
                .build();

        // НАСТРОЙКА МОКОВ
        //
        // Успешно вытаскиваем старого пользователя из базы
        Mockito.when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(oldUser));
        // Новый email уже принадлежит другому человеку в системе
        Mockito.when(userRepositoryMock.findByEmail(newEmail)).thenReturn(Optional.of(anotherUser));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> userService.update(userId, updateRequest))
                .isInstanceOf(DataIntegrityConflictException.class)
                .hasMessage(String.format("Email %s уже занят", newEmail));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(userId);
        // Обратились в userRepositoryMock.findByEmail только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findByEmail(newEmail);
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }

    @DisplayName("Ошибка NotFoundException, если обновляемый пользователь отсутствует в базе данных")
    @Test
    void throwExceptionWhenUserNotFoundDuringUpdate() {
        // ПОДГОТОВКА ДАННЫХ
        //
        Long wrongUserId = -1L;
        UserDtoUpdateRequest updateRequest = new UserDtoUpdateRequest();
        updateRequest.setName(newName);

        // НАСТРОЙКА МОКОВ
        //
        // Пользователь не найден в базе данных
        Mockito.when(userRepositoryMock.findById(wrongUserId)).thenReturn(Optional.empty());

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> userService.update(wrongUserId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format("Пользователь с id=%d не найден", wrongUserId));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userRepositoryMock.findById только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findById(wrongUserId);
        // Больше не обращались в другие методы этого репозитория
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }
}