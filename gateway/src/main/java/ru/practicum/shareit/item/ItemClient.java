package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX)).build());
    }

    public ResponseEntity<Object> create(Long userId, ItemDtoCreateRequest request) {
        return post("", userId, request);
    }

    public ResponseEntity<Object> createComment(Long userId, Long itemId, CommentRequestDto request) {
        return post(String.format("/%d/comment", itemId), userId, request);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, ItemDtoUpdateRequest request) {
        return patch("/" + itemId, userId, request);
    }

    public ResponseEntity<Object> findById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> findAllByOwnerId(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> searchByNameOrDescription(String text) {
        Map<String, Object> parameters = Map.of("text", text);

        return get("/search?text={text}", null, parameters);
    }
}
