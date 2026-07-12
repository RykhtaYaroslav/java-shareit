package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserGatewayValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @DisplayName("POST /users: Ошибка 400 при пустом имени пользователя")
    @Test
    void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {
        UserDtoCreateRequest badRequest = UserDtoCreateRequest.builder()
                .name("") // Пустое имя
                .email("user@email.com")
                .build();

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /users: Ошибка 400 при некорректном формате Email")
    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        UserDtoCreateRequest badRequest = UserDtoCreateRequest.builder()
                .name("User Name")
                .email("invalid-email-format") // Без собаки и домена
                .build();

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("PATCH /users/{userId}: Ошибка 400 при обновлении с некорректным форматом Email")
    @Test
    void shouldReturnBadRequestWhenUpdatingEmailIsInvalid() throws Exception {
        UserDtoUpdateRequest badRequest = new UserDtoUpdateRequest();
        badRequest.setEmail("bad@@email..com");

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /users/{userId}: Ошибка 400 при отрицательном или нулевом userId")
    @Test
    void shouldReturnBadRequestWhenUserIdIsNegativeOrZero() throws Exception {
        mockMvc.perform(get("/users/0") // Не пройдет проверку @Positive
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/-5") // Не пройдет проверку @Positive
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}