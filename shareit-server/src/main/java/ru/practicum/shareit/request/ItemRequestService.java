package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoCreateRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDtoCreateRequest dto);

    List<ItemRequestDto> findAllByUserId(Long userId);

    List<ItemRequestDto> findAllFromOthers(Long userId, Integer from, Integer size);

    ItemRequestDto findById(Long userId, Long requestId);
}
