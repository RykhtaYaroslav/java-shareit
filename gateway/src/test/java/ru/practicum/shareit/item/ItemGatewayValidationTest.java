package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemGatewayValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @DisplayName("POST /items: Ошибка 400 при пустом имени вещи")
    @Test
    void shouldReturnBadRequestWhenItemNameIsEmpty() throws Exception {
        ItemDtoCreateRequest badRequest = new ItemDtoCreateRequest();
        badRequest.setName(""); // Пусто
        badRequest.setDescription("Description");
        badRequest.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /items: Ошибка 400 при пустом описании вещи")
    @Test
    void shouldReturnBadRequestWhenItemDescriptionIsEmpty() throws Exception {
        ItemDtoCreateRequest badRequest = new ItemDtoCreateRequest();
        badRequest.setName("Name");
        badRequest.setDescription(""); // Пусто
        badRequest.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /items: Ошибка 400, если поле available равно null")
    @Test
    void shouldReturnBadRequestWhenItemAvailableIsNull() throws Exception {
        ItemDtoCreateRequest badRequest = new ItemDtoCreateRequest();
        badRequest.setName("Name");
        badRequest.setDescription("Description");
        badRequest.setAvailable(null); // Null

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /items/{itemId}/comment: Ошибка 400 при пустом тексте комментария")
    @Test
    void shouldReturnBadRequestWhenCommentTextIsEmpty() throws Exception {
        CommentRequestDto badRequest = CommentRequestDto.builder()
                .text("") // Пусто
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}