package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DataIntegrityConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }

        User user = userOptional.get();

        User toUpdate = UserDtoUpdateRequest.mapToModel(userId, updateRequest);

        updateUserFields(user, toUpdate);

        User updated = userRepository.save(toUpdate);
        return UserDto.mapToDto(updated);
    }

    @Override
    public void deleteById(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto findById(Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }
        return UserDto.mapToDto(user.get());
    }

    @Override
    public List<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserDto::mapToDto).toList();
    }

    private void checkEmailAvailability(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DataIntegrityConflictException(String.format("Email %s уже занят", email));
        }
    }

    private void updateUserFields(User user, User updateRequest) {
        if (updateRequest.getName() != null) {
            user.setName(updateRequest.getName());
        }

        String email = updateRequest.getEmail();

        if (email != null) {
            checkEmailAvailability(email);
            user.setEmail(email);
        }
    }
}
