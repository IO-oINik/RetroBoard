package ru.edu.retro.apiservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.edu.retro.apiservice.exceptions.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.edu.retro.apiservice.exceptions.EntityNotFoundException;
import ru.edu.retro.apiservice.mappers.UserMapper;
import ru.edu.retro.apiservice.models.dto.requests.UserEditRequest;
import ru.edu.retro.apiservice.models.dto.responses.UserResponse;
import ru.edu.retro.apiservice.repositories.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    public UserResponse findById(long userId) {
        log.info("Finding user by ID: {}", userId);
        var userResponse = userRepository.findById(userId)
                .map(mapper::toUserResponse)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new EntityNotFoundException("User not found with the ID: " + userId);
                });
        log.info("User found with ID: {}", userId);
        return userResponse;
    }

    public UserResponse findByLogin(String login) {
        log.info("Finding user by login: {}", login);
        var userResponse = userRepository.findByLogin(login)
                .map(mapper::toUserResponse)
                .orElseThrow(() -> {
                    log.warn("User not found with login: {}", login);
                    return new EntityNotFoundException("User not found with the login: " + login);
                });
        log.info("User found with login: {}", login);
        return userResponse;
    }

    public UserResponse findMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login;
        if (authentication != null) {
            login = (String) authentication.getPrincipal();
            log.info("Fetching current user details for login: {}", login);
        } else {
            log.error("Unauthorized access attempt to findMe");
            throw new AuthException("Unauthorized");
        }
        return findByLogin(login);
    }

    public UserResponse editMe(UserEditRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login;
        if (authentication != null) {
            login = (String) authentication.getPrincipal();
            log.info("Editing current user profile for login: {}", login);
        } else {
            log.error("Unauthorized access attempt to editMe");
            throw new AuthException("Unauthorized");
        }
        var user = userRepository.findByLogin(login).orElseThrow(() -> {
            log.warn("User not found with login: {}", login);
            return new EntityNotFoundException("User not found with the login: " + login);
        });
        return update(user.getId(), request);
    }

    public UserResponse update(long id, UserEditRequest request) {
        log.info("Updating user with ID: {}", id);
        var user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found with ID: {}", id);
            return new EntityNotFoundException("User not found with the ID: " + id);
        });

        if (!request.nickname().equals(user.getNickname())) {
            if (!userRepository.existsByNickname(request.nickname())) {
                log.info("Changing nickname for user ID {} from '{}' to '{}'", id, user.getNickname(), request.nickname());
                user.setNickname(request.nickname());
            } else {
                log.warn("Nickname '{}' already exists, cannot update user ID {}", request.nickname(), id);
            }
        }

        var savedUser = userRepository.save(user);
        log.info("User with ID {} updated successfully", id);
        return mapper.toUserResponse(savedUser);
    }

    public void delete(long id) {
        log.debug("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
        log.info("User with ID {} deleted", id);
    }
}