package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByUserIdOrderByCreatedDesc(Long userId);

    @Query(value = """
            SELECT *
            FROM requests
            WHERE requests.user_id != :userId
            ORDER BY creared DESC
            LIMIT :size
            OFFSET :from
            """, nativeQuery = true)
    List<ItemRequest> findAllByUserIdNotWithPagination(@Param("userId") Long userId,
                                                       @Param("from") Integer from,
                                                       @Param("size") Integer size);
}
