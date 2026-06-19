package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DataIntegrityConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDtoCreateRequest createRequest) {
        checkEmailAvailability(createRequest.getEmail());

        User forSave = UserDtoCreateRequest.mapToModel(createRequest);

        User saved = userRepository.save(forSave);
        return UserDto.mapToDto(saved);
    }

    @Override
    public UserDto update(Long userId, UserDtoUpdateRequest updateRequest) {
        User user = getUser(userId);

        User toUpdate = UserDtoUpdateRequest.mapToModel(userId, updateRequest);

        updateUserFields(user, toUpdate);

        User updated = userRepository.save(user);
        return UserDto.mapToDto(updated);
    }

    @Override
    public void deleteById(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long userId) {
        User user = getUser(userId);
        return UserDto.mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserDto::mapToDto).toList();
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));
    }

    private void checkEmailAvailability(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DataIntegrityConflictException(String.format("Email %s уже занят", email));
        }
    }

    private void updateUserFields(User user, User toUpdate) {
        if (toUpdate.getName() != null) {
            user.setName(toUpdate.getName());
        }

        String email = toUpdate.getEmail();

        if (email != null) {
            checkEmailAvailability(email);
            user.setEmail(email);
        }
    }
}
