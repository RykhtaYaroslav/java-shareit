package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestRequestDto dto) {
        User user = userService.getUser(userId);

        LocalDateTime created = LocalDateTime.now();

        ItemRequest itemRequest = itemRequestMapper.mapToModel(dto, created, user);
        itemRequest = itemRequestRepository.save(itemRequest);

        List<ItemDto> itemsDto = Collections.emptyList();

        return itemRequestMapper.mapToDto(itemRequest, itemsDto);
    }
}
