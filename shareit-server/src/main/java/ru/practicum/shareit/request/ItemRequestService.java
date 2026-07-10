package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestCreateRequestDto dto);

    List<ItemRequestDto> findAllByUserId(Long userId);

    List<ItemRequestDto> findAllFromOthers(Long userId, Integer from, Integer size);

    ItemRequestDto findById(Long userId, Long requestId);
}
