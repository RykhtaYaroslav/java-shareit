package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderByIdAsc(Long userId);

    List<Item> findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameText, String descText);

    List<Item> findAllByItemRequestIdIn(List<Long> requestId);
}
