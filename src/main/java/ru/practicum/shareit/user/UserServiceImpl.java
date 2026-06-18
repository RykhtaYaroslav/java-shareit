package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DataIntegrityConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;
import ru.practicum.shareit.user.dto.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDtoCreateRequest createRequest) {
        checkEmailAvailability(createRequest.getEmail());

        User toCreate = UserMapper.mapToModel(createRequest);

        Long id = userRepository.create(toCreate);
        toCreate.setId(id);
        return UserMapper.mapToDto(toCreate);
    }

    @Override
    public UserDto update(Long userId, UserDtoUpdateRequest updateRequest) {
        checkUserExistence(userId);
        checkEmailAvailability(updateRequest.getEmail());

        User toUpdate = UserMapper.mapToModel(userId, updateRequest);

        User user = userRepository.update(userId, toUpdate);
        return UserMapper.mapToDto(user);
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
        return UserMapper.mapToDto(user.get());
    }

    @Override
    public List<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::mapToDto).toList();
    }

    private void checkUserExistence(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }
    }

    private void checkEmailAvailability(String email) {
        if (!userRepository.isUniqueEmail(email)) {
            throw new DataIntegrityConflictException(String.format("Email %s уже занят", email));
        }
    }
}
