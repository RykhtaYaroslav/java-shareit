package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @DisplayName("Сериализация ItemDto в JSON")
    @Test
    void testSerialize() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Comment Text")
                .authorName("Author Name")
                .created(LocalDateTime.of(2026, 7, 12, 12, 0, 0))
                .build();

        ItemDto.BookingShortDto lastBooking = ItemDto.BookingShortDto.builder()
                .id(5L)
                .bookerId(2L)
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .requestId(100L)
                .lastBooking(lastBooking)
                .nextBooking(null)
                .comments(List.of(commentDto))
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        // Проверка базовых полей
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Item Name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Item Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(100);

        // Проверка вложенного объекта бронирования
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();

        // Проверка списка комментариев
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Comment Text");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Author Name");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created").isEqualTo("2026-07-12T12:00:00");
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @DisplayName("Десериализация JSON в ItemDto")
    @Test
    void testDeserialize() throws Exception {
        String jsonContent = """
                {
                  "id": 1,
                  "name": "Item Name",
                  "description": "Item Description",
                  "available": true,
                  "requestId": 100,
                  "comments": []
                }
                """;

        ItemDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Item Name");
        assertThat(dto.getDescription()).isEqualTo("Item Description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(100L);
        assertThat(dto.getComments()).isEmpty();
    }
}