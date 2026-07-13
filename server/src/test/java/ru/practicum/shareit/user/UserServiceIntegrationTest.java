package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class UserServiceIntegrationTest {
    private final UserService userService;

    private static final String TEST_NAME = "Test Name";
    private static final String TEST_EMAIL = "Test@email.test";

    private static final UserDtoCreateRequest CREATE_REQUEST = UserDtoCreateRequest.builder().name(TEST_NAME).email(TEST_EMAIL).build();
    private static final UserDtoCreateRequest NOISE_USER = UserDtoCreateRequest.builder().name("Noise").email("Noise@noise.noise").build();

    private static final String NEW_NAME = "New Name";
    private static final String NEW_EMAIL = "New@email.test";

    @DisplayName("Успешное создание нового юзера")

    @Test
    void shouldCreateUser() {
        UserDto noise = userService.create(NOISE_USER);
        UserDto createdDto = userService.create(CREATE_REQUEST);

        assertThat(createdDto.getId()).isNotNull().isPositive();

        UserDto foundById = userService.findById(createdDto.getId());

        assertThat(createdDto).isEqualTo(foundById);

        assertThat(createdDto.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(createdDto.getName()).isEqualTo(TEST_NAME);
        assertThat(noise.getId()).isNotEqualTo(createdDto.getId());
    }

    @DisplayName("Успешное обновление данных пользователя")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUpdateData")
    void shouldUpdateUserFields(
            String nameOfTest,
            UserDtoUpdateRequest request,
            String expectedName,
            String expectedEmail) {
        UserDto noiseUser = userService.create(NOISE_USER);
        UserDto oldUser = userService.create(CREATE_REQUEST);
        UserDto newUser = userService.update(oldUser.getId(), request);

        assertThat(newUser.getName()).isEqualTo(expectedName);
        assertThat(newUser.getEmail()).isEqualTo(expectedEmail);
        assertThat(newUser.getId()).isEqualTo(oldUser.getId());
        assertThat(noiseUser.getId()).isNotEqualTo(newUser.getId());
    }

    @DisplayName("Возвращение списка всех юзеров")
    @Test
    void shouldReturnListOfAllUsers() {
        int usersAmount = 2;
        UserDto first = userService.create(NOISE_USER);
        UserDto second = userService.create(CREATE_REQUEST);

        List<UserDto> users = userService.findAll();

        assertThat(users).hasSize(usersAmount);
        assertThat(users.getFirst()).isEqualTo(first);
        assertThat(users.get(1)).isEqualTo(second);
    }

    private static Stream<Arguments> getUpdateData() {
        return Stream.of(
                Arguments.of("Обновление имени", getUpdates(NEW_NAME, null), NEW_NAME, TEST_EMAIL),
                Arguments.of("Обновление почты", getUpdates(null, NEW_EMAIL), TEST_NAME, NEW_EMAIL),
                Arguments.of("Обновление имени и почты", getUpdates(NEW_NAME, NEW_EMAIL), NEW_NAME, NEW_EMAIL),
                Arguments.of("Ничего не обновляется", getUpdates(null, null), TEST_NAME, TEST_EMAIL)
        );
    }

    private static UserDtoUpdateRequest getUpdates(String name, String email) {
        UserDtoUpdateRequest request = new UserDtoUpdateRequest();
        request.setName(name);
        request.setEmail(email);

        return request;
    }
}
