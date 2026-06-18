package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserInMemoryRepository implements UserRepository {
    private final AtomicLong currentMaxId;
    private final Map<Long, User> users;

    public UserInMemoryRepository() {
        this.users = new ConcurrentHashMap<>();
        this.currentMaxId = new AtomicLong(0L);
    }


    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Long create(User userCreateRequest) {
        Long id = currentMaxId.incrementAndGet();
        userCreateRequest.setId(id);
        users.put(id, userCreateRequest);
        return id;
    }

    @Override
    public User update(Long userId, User userUpdateRequest) {
        User oldUser = users.get(userId);
        updateUserFields(userUpdateRequest, oldUser);
        return oldUser;
    }

    @Override
    public void deleteById(Long userId) {
        users.remove(userId);
    }

    @Override
    public boolean isUniqueEmail(String email) {
        Optional<User> user = users.values().stream().filter(u -> u.getEmail().equals(email)).findAny();
        return user.isEmpty();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    private void updateUserFields(User userUpdateRequest, User oldUser) {
        if (userUpdateRequest.getName() != null && !userUpdateRequest.getName().isBlank()) {
            oldUser.setName(userUpdateRequest.getName());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().isBlank()) {
            oldUser.setEmail(userUpdateRequest.getEmail());
        }
    }
}
