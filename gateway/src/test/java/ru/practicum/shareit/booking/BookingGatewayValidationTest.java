package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingGatewayValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @DisplayName("POST /bookings: Ошибка 400, если itemId отсутствует")
    @Test
    void shouldReturnBadRequestWhenItemIdIsNull() throws Exception {
        BookingRequestDto badRequest = BookingRequestDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /bookings: Ошибка 400, если дата старта в прошлом")
    @Test
    void shouldReturnBadRequestWhenStartDateIsInPast() throws Exception {
        BookingRequestDto badRequest = BookingRequestDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /bookings: Ошибка 400 при передаче неизвестного значения state")
    @Test
    void shouldReturnBadRequestWhenStateIsUnknown() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "unsupported") // Невалидный стейт
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: unsupported"));
    }
}