package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestRequestDto dto);
}
