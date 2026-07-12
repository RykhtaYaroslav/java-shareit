package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestGatewayValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @DisplayName("POST /requests: Ошибка 400 при пустом описании запроса")
    @Test
    void shouldReturnBadRequestWhenDescriptionIsEmpty() throws Exception {
        ItemRequestRequestDto badRequest = new ItemRequestRequestDto();
        badRequest.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /requests/all: Ошибка 400 при отрицательном индексе from")
    @Test
    void shouldReturnBadRequestWhenFromIsNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /requests/all: Ошибка 400 при нулевом или отрицательном размере страницы size")
    @Test
    void shouldReturnBadRequestWhenSizeIsZeroOrNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}