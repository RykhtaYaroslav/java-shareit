package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoCreateRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private ItemRequestDto itemRequestDto;
    private ItemRequestDtoCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new ItemRequestDtoCreateRequest();
        createRequest.setDescription("Request Description");

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Request Description")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();
    }

    @DisplayName("create(): Успешное создание запроса на вещь")
    @Test
    void shouldCreateItemRequest() throws Exception {
        when(itemRequestService.create(eq(1L), any(ItemRequestDtoCreateRequest.class))).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(createRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.items").isArray());
    }

    @DisplayName("findAllByUserId(): Успешное получение списка своих запросов")
    @Test
    void shouldReturnAllRequestsByUserId() throws Exception {
        when(itemRequestService.findAllByUserId(eq(1L))).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()));
    }

    @DisplayName("findAllFromOthers(): Успешное получение чужих запросов с пагинацией")
    @Test
    void shouldReturnRequestsFromOthersWithPagination() throws Exception {
        when(itemRequestService.findAllFromOthers(eq(1L), anyInt(), anyInt())).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()));
    }

    @DisplayName("findById(): Успешное получение запроса по его ID")
    @Test
    void shouldReturnItemRequestById() throws Exception {
        when(itemRequestService.findById(1L, 1L)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/1")
                        .header(USER_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()));
    }
}