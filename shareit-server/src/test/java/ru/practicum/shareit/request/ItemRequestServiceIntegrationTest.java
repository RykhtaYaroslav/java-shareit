package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoCreateRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {
    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemService itemService;

    private static final String DESCRIPTION = "Description";


    @DisplayName("Успешное создание запроса на Item и получение запроса с ответом")
    @Test
    void shouldCreateItemRequest() {
        TestContext context = createTestContext();
        UserDto owner = context.owner();
        UserDto requester = context.requester();

        LocalDateTime now = LocalDateTime.now();

        // Create request for item
        ItemRequestDtoCreateRequest requestRequestDto = new ItemRequestDtoCreateRequest();
        requestRequestDto.setDescription(DESCRIPTION);

        ItemRequestDto itemRequestDto = itemRequestService.create(requester.getId(), requestRequestDto);

        // Create response for request
        ItemDtoCreateRequest itemDtoCreateRequest = getItemDto(itemRequestDto);

        ItemDto itemDto = itemService.create(owner.getId(), itemDtoCreateRequest);

        // Find itemRequest with Response
        ItemRequestDto result = itemRequestService.findById(requester.getId(), itemRequestDto.getId());

        assertThat(itemDto.getRequestId()).isEqualTo(itemRequestDto.getId());
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreated()).isAfterOrEqualTo(now);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getId()).isEqualTo(itemDto.getId());
    }

    @DisplayName("Метод findAllByUserId возвращает только запросы самого пользователя")
    @Test
    void shouldReturnOnlyRequestsByUserId() {
        TestContext context = createTestContext();
        UserDto requester1 = context.owner();
        UserDto requester2 = context.requester();

        ItemRequestDto itemRequestDto1 = createRequest(requester1.getId());
        ItemRequestDto itemRequestDto2 = createRequest(requester2.getId());

        List<ItemRequestDto> itemRequestDtos1 = itemRequestService.findAllByUserId(requester1.getId());
        List<ItemRequestDto> itemRequestDtos2 = itemRequestService.findAllByUserId(requester2.getId());

        assertThat(itemRequestDtos1).hasSize(1).containsExactly(itemRequestDto1);
        assertThat(itemRequestDtos2).hasSize(1).containsExactly(itemRequestDto2);
    }

    @DisplayName("Метод findAllFromOthers возвращает запросы других юзеров")
    @Test
    void shouldReturnOnlyRequestsFromOtherUsers() {
        TestContext context = createTestContext();
        UserDto requester1 = context.owner();
        UserDto requester2 = context.requester();

        ItemRequestDto itemRequestDto1 = createRequest(requester1.getId());
        ItemRequestDto itemRequestDto2 = createRequest(requester2.getId());

        List<ItemRequestDto> itemRequestDtos1 = itemRequestService.findAllFromOthers(requester1.getId(), 0, 10);
        List<ItemRequestDto> itemRequestDtos2 = itemRequestService.findAllFromOthers(requester2.getId(), 0, 10);

        assertThat(itemRequestDtos1).hasSize(1).containsExactly(itemRequestDto2);
        assertThat(itemRequestDtos2).hasSize(1).containsExactly(itemRequestDto1);
    }


    private ItemRequestDto createRequest(Long id) {
        // Create requests for item
        ItemRequestDtoCreateRequest request = new ItemRequestDtoCreateRequest();
        request.setDescription(DESCRIPTION + id);
        return itemRequestService.create(id, request);

    }

    private ItemDtoCreateRequest getItemDto(ItemRequestDto itemRequestDto) {
        ItemDtoCreateRequest itemDtoCreateRequest = new ItemDtoCreateRequest();
        itemDtoCreateRequest.setItemRequestId(itemRequestDto.getId());
        itemDtoCreateRequest.setAvailable(true);
        itemDtoCreateRequest.setDescription(DESCRIPTION);
        itemDtoCreateRequest.setName("ItemForRequest");
        return itemDtoCreateRequest;
    }

    private TestContext createTestContext() {
        String ownerName = "Owner";
        String ownerEmail = "owner@email.owner";
        String requesterName = "Requester";
        String requesterEmail = "requester@email.requester";

        UserDto owner = userService.create(UserDtoCreateRequest.builder().name(ownerName).email(ownerEmail).build());
        UserDto requester = userService.create(UserDtoCreateRequest.builder().name(requesterName).email(requesterEmail).build());

        return new TestContext(owner, requester);
    }

    private record TestContext(UserDto owner, UserDto requester) {
    }
}
