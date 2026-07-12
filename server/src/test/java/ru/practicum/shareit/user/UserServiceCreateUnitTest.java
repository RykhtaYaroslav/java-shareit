package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataIntegrityConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserServiceCreateUnitTest {
    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private UserServiceImpl userService;

    private final Long userId = 1L;
    private final String email = "user@mail.com";
    private final String name = "User Name";

    private final UserDtoCreateRequest createRequest = UserDtoCreateRequest.builder()
            .name(name)
            .email(email)
            .build();

    private final User savedUser = User.builder()
            .id(userId)
            .name(name)
            .email(email)
            .build();

    @DisplayName("Успешное создание пользователя, если email свободен")
    @Test
    void shouldCreateUserSuccessfully() {
        // НАСТРОЙКА МОКОВ
        //
        // Email свободен, репозиторий возвращает Optional.empty()
        Mockito.when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.empty());
        // Успешное сохранение в базу данных
        Mockito.when(userRepositoryMock.save(Mockito.any(User.class))).thenReturn(savedUser);

        // ВЫЗОВ МЕТОДА
        //
        UserDto result = userService.create(createRequest);
        UserDto expected = UserDto.mapToDto(savedUser);

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        assertThat(result).isEqualTo(expected);
        // Обратились в userRepositoryMock.findByEmail только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findByEmail(email);
        // Обратились в userRepositoryMock.save только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(Mockito.any(User.class));
        // Больше не обращались в другие методы этого репозитория:
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }

    @DisplayName("Ошибка DataIntegrityConflictException, если email уже занят")
    @Test
    void throwExceptionWhenEmailIsAlreadyTaken() {
        // НАСТРОЙКА МОКОВ
        //
        // Email занят — репозиторий возвращает существующего пользователя
        Mockito.when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.of(savedUser));

        // ВЫЗОВ МЕТОДА И ПРОВЕРКА ИСКЛЮЧЕНИЯ
        //
        assertThatThrownBy(() -> userService.create(createRequest))
                .isInstanceOf(DataIntegrityConflictException.class)
                .hasMessage(String.format("Email %s уже занят", email));

        // ПРОВЕРКА РЕЗУЛЬТАТОВ
        //
        // Обратились в userRepositoryMock.findByEmail только один раз
        Mockito.verify(userRepositoryMock, Mockito.times(1)).findByEmail(email);
        // Больше не обращались в другие методы этого репозитория:
        Mockito.verifyNoMoreInteractions(userRepositoryMock);
    }
}