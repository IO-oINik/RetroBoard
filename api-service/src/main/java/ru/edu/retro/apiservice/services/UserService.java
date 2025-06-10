package ru.edu.retro.apiservice.services;

import lombok.RequiredArgsConstructor;
import ru.edu.retro.apiservice.exceptions.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.edu.retro.apiservice.exceptions.EntityNotFoundException;
import ru.edu.retro.apiservice.mappers.UserMapper;
import ru.edu.retro.apiservice.models.dto.requests.UserEditRequest;
import ru.edu.retro.apiservice.models.dto.responses.UserResponse;
import ru.edu.retro.apiservice.repositories.UserRepository;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    public UserResponse findById(long userId) {
        return userRepository.findById(userId)
                .map(mapper::toUserResponse)
                .orElseThrow(() -> new EntityNotFoundException("User not found with the ID: " + userId));
    }

    public UserResponse findByLogin(String login) {
        return userRepository.findByLogin(login)
                .map(mapper::toUserResponse)
                .orElseThrow(() -> new EntityNotFoundException("User not found with the login: " + login));
    }

    public UserResponse findMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login;
        if (authentication != null) {
            login = (String) authentication.getPrincipal();
        } else {
            throw new AuthException("Unauthorized");
        }
        return findByLogin(login);
    }

    public UserResponse editMe(UserEditRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login;
        if (authentication != null) {
            login = (String) authentication.getPrincipal();
        } else {
            throw new AuthException("Unauthorized");
        }
        var user = userRepository.findByLogin(login).orElseThrow(() -> new EntityNotFoundException("User not found with the login: " + login));
        return update(user.getId(), request);
    }

    public UserResponse update(long id, UserEditRequest request) {
        var user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with the ID: " + id));

        if(!request.nickname().equals(user.getNickname()) && !userRepository.existsByNickname(request.nickname())) {
            user.setNickname(request.nickname());
        }

        return mapper.toUserResponse(userRepository.save(user));
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }
}
