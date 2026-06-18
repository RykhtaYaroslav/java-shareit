package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemInMemoryRepository implements ItemRepository {
    private final AtomicLong currentMaxId = new AtomicLong(0L);
    private final Map<Long, Item> items = new ConcurrentHashMap<>();

    @Override
    public Item create(Item itemCreateRequest) {
        Long newItemId = getNextId();

        itemCreateRequest.setId(newItemId);
        items.put(newItemId, itemCreateRequest);
        return itemCreateRequest;
    }

    @Override
    public Item update(Item itemUpdateRequest) {
        Item oldItem = items.get(itemUpdateRequest.getId());

        updateItemFields(itemUpdateRequest, oldItem);
        return oldItem;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findAllByUserId(Long userId) {
        return items.values().stream()
                .filter(item -> item.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        String lowerCaseText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerCaseText)
                        || item.getDescription().toLowerCase().contains(lowerCaseText))
                .collect(Collectors.toList());
    }

    private Long getNextId() {
        return currentMaxId.incrementAndGet();
    }

    private void updateItemFields(Item itemUpdateRequest, Item oldItem) {
        if (itemUpdateRequest.getName() != null && !itemUpdateRequest.getName().isBlank()) {
            oldItem.setName(itemUpdateRequest.getName());
        }
        if (itemUpdateRequest.getDescription() != null && !itemUpdateRequest.getDescription().isBlank()) {
            oldItem.setDescription(itemUpdateRequest.getDescription());
        }
        if (itemUpdateRequest.getAvailable() != null) {
            oldItem.setAvailable(itemUpdateRequest.getAvailable());
        }
    }
}
