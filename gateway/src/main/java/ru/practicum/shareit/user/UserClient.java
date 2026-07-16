package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(
                restTemplateBuilder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> create(UserDtoCreateRequest request) {
        return post("", request);
    }

    public ResponseEntity<Object> update(Long userId, UserDtoUpdateRequest request) {
        return patch("/" + userId, request);
    }

    public ResponseEntity<Object> findById(Long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> findAll() {
        return get("");
    }

    public void deleteById(Long userId) {
        delete("/" + userId);
    }


}
