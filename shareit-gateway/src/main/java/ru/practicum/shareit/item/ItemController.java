package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemDtoCreateRequest request) {
        return itemClient.create(userId, request);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId,
                                                @RequestBody CommentRequestDto request) {
        return itemClient.createComment(userId, itemId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDtoUpdateRequest request) {
        return itemClient.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable Long itemId) {
        return itemClient.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.findAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findAllByNameOrDescriptionContainingIgnoreCase(@RequestParam String text) {
        return itemClient.searchByNameOrDescription(text);
    }
}
