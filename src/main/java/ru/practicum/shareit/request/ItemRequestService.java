package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestRequestDto dto);

    List<ItemRequestDto> findAllByUserId(Long userId);

    List<ItemRequestDto> findAllFromOthers(Long userId, Integer from, Integer size);
}
