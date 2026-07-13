package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

@Data
public class ItemDtoCreateRequest {
    private String name;

    private String description;

    private Boolean available;

    @JsonAlias("requestId")
    @JsonProperty("requestId")
    private Long itemRequestId;

    public static Item mapToModel(Long userId, ItemDtoCreateRequest request) {
        return Item.builder()
                .ownerId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .itemRequestId(request.getItemRequestId())
                .build();
    }
}
