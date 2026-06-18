package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long userId);

    Long create(User userCreateRequest);

    User update(Long userId, User userUpdateRequest);

    void deleteById(Long userId);

    boolean isUniqueEmail(String email);

    List<User> findAll();
}
