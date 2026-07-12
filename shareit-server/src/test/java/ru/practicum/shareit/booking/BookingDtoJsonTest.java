package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest // Аннотация изолирует тестирование только JSON-слоя Jackson
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @DisplayName("Сериализация BookingDto в JSON")
    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 7, 12, 14, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 12, 18, 0, 0);

        BookingDto.ItemShortDto item = BookingDto.ItemShortDto.builder()
                .id(10L)
                .name("Item Name")
                .build();

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status("WAITING")
                .item(item)
                .build();

        // Превращаем объект в JSON-контент
        JsonContent<BookingDto> result = json.write(dto);

        // Проверяем наличие полей в JSON-строке с помощью JsonPath выражений
        assertThat(result).hasJsonPathNumberValue("$.id")
                .hasJsonPathStringValue("$.start")
                .hasJsonPathStringValue("$.end")
                .hasJsonPathStringValue("$.status");

        // Проверяем конкретные значения полей
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Item Name");

        // Проверяем, что дата сериализовалась в стандартный ISO формат (строку)
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-07-12T14:00:00");
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @DisplayName("Десериализация JSON в BookingDto")
    @Test
    void testDeserialize() throws Exception {
        String jsonContent = """
                {
                  "id": 1,
                  "start": "2026-07-12T14:00:00",
                  "end": "2026-07-12T18:00:00",
                  "status": "APPROVED"
                }
                """;

        // Превращаем строку в Java-объект
        BookingDto dto = json.parse(jsonContent).getObject();

        // Проверяем корректность маппинга полей
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo("APPROVED");
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 7, 12, 14, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 7, 12, 18, 0, 0));
    }
}