package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CommentService commentService;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private ItemDto itemDto;
    private ItemDtoCreateRequest createRequest;
    private ItemDtoUpdateRequest updateRequest;
    private CommentDto commentDto;
    private CommentDtoCreateRequest commentRequest;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .build();

        createRequest = new ItemDtoCreateRequest();
        createRequest.setName("Item Name");
        createRequest.setDescription("Item Description");
        createRequest.setAvailable(true);

        updateRequest = new ItemDtoUpdateRequest();
        updateRequest.setName("Updated Item Name");

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Comment Text")
                .authorName("Author Name")
                .created(LocalDateTime.now())
                .build();

        commentRequest = CommentDtoCreateRequest.builder()
                .text("Comment Text")
                .build();
    }

    @DisplayName("create(): Успешное добавление вещи")
    @Test
    void shouldCreateItem() throws Exception {
        when(itemService.create(eq(1L), any(ItemDtoCreateRequest.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(createRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @DisplayName("update(): Успешное обновление полей вещи")
    @Test
    void shouldUpdateItem() throws Exception {
        itemDto.setName("Updated Item Name");
        when(itemService.update(eq(1L), eq(1L), any(ItemDtoUpdateRequest.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Item Name"));
    }

    @DisplayName("findById(): Успешное получение вещи по ID")
    @Test
    void shouldReturnItemById() throws Exception {
        when(itemService.findById(1L, 1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header(USER_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));
    }

    @DisplayName("findAllByUserId(): Успешное получение списка вещей владельца")
    @Test
    void shouldReturnItemsByOwner() throws Exception {
        when(itemService.findAllByOwnerId(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()));
    }

    @DisplayName("search(): Успешный текстовый поиск вещей")
    @Test
    void shouldSearchItemsByText() throws Exception {
        when(itemService.searchByNameOrDescription(anyString())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "search query")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()));
    }

    @DisplayName("createComment(): Успешное добавление отзыва к вещи")
    @Test
    void shouldCreateComment() throws Exception {
        when(commentService.createComment(eq(1L), eq(1L), any(CommentDtoCreateRequest.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .content(mapper.writeValueAsString(commentRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()));
    }
}