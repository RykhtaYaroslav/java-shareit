package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ItemRequestRequestDto requestDto) {
        return itemRequestService.create(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestDto> findAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findAllByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllFromOthers(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return itemRequestService.findAllFromOthers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findById(@PathVariable Long requestId,
                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findById(userId, requestId);
    }
}
