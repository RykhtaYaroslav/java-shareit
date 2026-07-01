package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceUpdateTest {
    @Mock
    private ItemRequestRepository itemRequestRepositoryMock;

    @Mock
    private ItemRepository itemRepositoryMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private BookingRepository bookingRepositoryMock;

    @Mock
    private CommentRepository commentRepositoryMock;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final Long itemId = 1L;
    private final Long itemRequestId = 1L;
    private final Long userId = 1L;

    private final String oldName = "Old Name";
    private final String newName = "New Name";
    private final String oldDescription = "Old Description";
    private final String newDescription ="New Description";
    private final boolean oldAvailable = true;
    private final boolean newAvailable = false;

    private final Item oldItem = new Item();

    private final User testUser = User.builder().id(userId).name("Owner").email("owner@mail.com").build();
    private final ItemDtoUpdateRequest testRequest = new ItemDtoUpdateRequest();

    @BeforeEach
    void getOldItem() {
        oldItem.setId(itemId);
        oldItem.setName(oldName);
        oldItem.setDescription(oldDescription);
        oldItem.setAvailable(oldAvailable);
    }



}
