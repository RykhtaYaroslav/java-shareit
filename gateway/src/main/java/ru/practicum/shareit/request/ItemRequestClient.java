package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX)).build());
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> findAllByUserId(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> findAllFromOthers(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> findById(Long requestId, Long userId) {
        return get("/" + requestId, userId);
    }

}
