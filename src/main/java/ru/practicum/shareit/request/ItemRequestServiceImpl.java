package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<ItemRequestDto.ItemResponseDto> itemsResponseDto = Collections.emptyList();

        return itemRequestMapper.mapToDto(itemRequest, itemsResponseDto);
    }

    @Override
    public List<ItemRequestDto> findAllByUserId(Long userId) {
        userService.getUser(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByUserIdOrderByCreatedDesc(userId);

        Map<Long, List<ItemRequestDto.ItemResponseDto>> responsesDto = getResponses(requests);


        return requests.stream().map(itemRequest -> {
            List<ItemRequestDto.ItemResponseDto> responsesById = responsesDto.getOrDefault(itemRequest.getId(), Collections.emptyList());
            return itemRequestMapper.mapToDto(itemRequest, responsesById);
        }).toList();
    }

    @Override
    public List<ItemRequestDto> findAllFromOthers(Long userId, Integer from, Integer size) {
        userService.findById(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByUserIdNotWithPagination(userId, from, size);

        Map<Long, List<ItemRequestDto.ItemResponseDto>> responsesDto = getResponses(requests);

        return requests.stream().map(itemRequest -> {
            List<ItemRequestDto.ItemResponseDto> responsesById = responsesDto.getOrDefault(itemRequest.getId(), Collections.emptyList());
            return itemRequestMapper.mapToDto(itemRequest, responsesById);
        }).toList();
    }

    private Map<Long, List<ItemRequestDto.ItemResponseDto>> getResponses(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();

        List<Item> items = itemService.findAllByRequestsIds(requestIds);

        return items.stream().collect(Collectors.groupingBy
                (Item::getRequestId, Collectors.mapping(
                                itemRequestMapper::mapToItemResponseDto, Collectors.toList()
                        )
                ));
    }
}
