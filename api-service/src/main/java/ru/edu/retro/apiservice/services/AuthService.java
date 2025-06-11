package ru.edu.retro.apiservice.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.edu.retro.apiservice.exceptions.EntityExistsException;
import ru.edu.retro.apiservice.exceptions.InvalidCredentialsException;
import ru.edu.retro.apiservice.exceptions.InvalidTokenException;
import ru.edu.retro.apiservice.mappers.UserMapper;
import ru.edu.retro.apiservice.models.db.RefreshToken;
import ru.edu.retro.apiservice.models.db.User;
import ru.edu.retro.apiservice.models.dto.requests.LoginRequest;
import ru.edu.retro.apiservice.models.dto.requests.RegisterRequest;
import ru.edu.retro.apiservice.models.dto.responses.TokenResponse;
import ru.edu.retro.apiservice.repositories.RefreshTokenRepository;
import ru.edu.retro.apiservice.repositories.UserRepository;
import ru.edu.retro.apiservice.utils.JwtUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public TokenResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.login());

        User user = userRepository.findByLogin(request.login())
                .orElseThrow(() -> {
                    log.debug("User not found: {}", request.login());
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.debug("Invalid password for user: {}", request.login());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getToken() != null) {
            log.debug("Existing refresh token found for user {}, removing", request.login());
            RefreshToken token = user.getToken();
            user.setToken(null);
            userRepository.save(user);
            refreshTokenRepository.delete(token);
        }

        TokenResponse response = generateTokens(user);
        log.info("User {} logged in successfully", request.login());
        return response;
    }

    public TokenResponse refresh(String refreshToken) {
        log.debug("Refreshing token");

        if (refreshToken == null) {
            log.debug("Refresh token is null");
            throw new InvalidTokenException("Invalid refresh token");
        }

        String login;
        try {
            login = jwtUtils.extractLogin(refreshToken);
        } catch (ExpiredJwtException ex) {
            log.warn("Refresh token expired");
            throw new InvalidTokenException("refresh token is expired");
        } catch (JwtException ex) {
            log.warn("Invalid refresh token: JWT parsing failed");
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in repository");
                    return new InvalidTokenException("refresh token is invalid");
                });

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.warn("User {} not found during token refresh", login);
                    return new InvalidTokenException("refresh token invalid");
                });

        if (!user.getToken().getToken().equals(refreshToken)) {
            log.warn("Refresh token mismatch for user {}", login);
            throw new InvalidTokenException("Token is invalid");
        }

        user.setToken(null);
        userRepository.save(user);
        refreshTokenRepository.delete(token);

        TokenResponse response = generateTokens(user);
        log.info("Tokens refreshed successfully for user {}", login);
        return response;
    }

    public TokenResponse register(RegisterRequest request) {
        log.info("Registering user: login={}, nickname={}", request.login(), request.nickname());

        if (userRepository.existsByNickname(request.nickname())) {
            log.debug("Nickname {} already exists", request.nickname());
            throw new EntityExistsException("User with the nickname: " + request.nickname() + " already exists");
        }

        if (userRepository.existsByLogin(request.login())) {
            log.debug("Login {} already exists", request.login());
            throw new EntityExistsException("User with the login: " + request.login() + " already exists");
        }

        User user = mapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        TokenResponse response = generateTokens(user);
        log.info("User {} registered successfully", request.login());
        return response;
    }

    public void logout(String refreshToken) {
        log.debug("Logging out using refresh token");

        String login;
        try {
            login = jwtUtils.extractLogin(refreshToken);
        } catch (ExpiredJwtException ex) {
            log.warn("Refresh token expired during logout");
            throw new InvalidTokenException("refresh token is expired");
        } catch (JwtException ex) {
            log.warn("Invalid refresh token during logout");
            throw new InvalidTokenException("Invalid refresh token");
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.warn("User {} not found during logout", login);
                    return new InvalidTokenException("refresh token invalid");
                });

        if (!user.getToken().getToken().equals(refreshToken)) {
            log.warn("Token mismatch during logout for user {}", login);
            throw new InvalidTokenException("Token is invalid");
        }

        RefreshToken token = user.getToken();
        user.setToken(null);
        userRepository.save(user);
        refreshTokenRepository.delete(token);
        log.info("User {} logged out successfully", login);
    }

    private TokenResponse generateTokens(User user) {
        String login = user.getLogin();
        log.debug("Generating tokens for user {}", login);

        String refreshToken = jwtUtils.generateRefreshToken(login);
        LocalDateTime expiresAtRefreshToken = LocalDateTime.ofInstant(
                jwtUtils.extractExpiresAt(refreshToken),
                ZoneId.systemDefault()
        );

        RefreshToken token = refreshTokenRepository.save(new RefreshToken(refreshToken, expiresAtRefreshToken));
        user.setToken(token);
        userRepository.save(user);

        String accessToken = jwtUtils.generateAccessToken(login, user.getId());

        log.debug("Tokens generated for user {}", login);
        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtUtils.extractExpiresAt(accessToken).toEpochMilli()
        );
    }
}
