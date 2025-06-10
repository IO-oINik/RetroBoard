package ru.edu.retro.apiservice.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.login()).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getToken() != null) {
            RefreshToken token = user.getToken();
            user.setToken(null);
            userRepository.save(user);
            refreshTokenRepository.delete(token);
        }

        return generateTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        String login;
        try {
            login = jwtUtils.extractLogin(refreshToken);
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("refresh token is expired");
        } catch (JwtException ex) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new InvalidTokenException("refresh token is invalid"));
        User user = userRepository.findByLogin(login).orElseThrow(() -> new InvalidTokenException("refresh token invalid"));
        if (!user.getToken().getToken().equals(refreshToken)) {
            throw new InvalidTokenException("Token is invalid");
        }
        user.setToken(null);
        userRepository.save(user);
        refreshTokenRepository.delete(token);

        return generateTokens(user);
    }

    public TokenResponse register(RegisterRequest request) {
        if(userRepository.existsByNickname(request.nickname())) {
            throw new EntityExistsException("User with the nickname: " + request.nickname() + " already exists");
        }
        if(userRepository.existsByLogin(request.login())) {
            throw new EntityExistsException("User with the login: " + request.login() + " already exists");
        }

        var user = mapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        return generateTokens(user);
    }

    public void logout(String refreshToken) {
        String login;
        try {
            login = jwtUtils.extractLogin(refreshToken);
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("refresh token is expired");
        } catch (JwtException ex) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        User user = userRepository.findByLogin(login).orElseThrow(() -> new InvalidTokenException("refresh token invalid"));

        if(!user.getToken().getToken().equals(refreshToken)) {
            throw new InvalidTokenException("Token is invalid");
        }
        var token = user.getToken();
        user.setToken(null);
        userRepository.save(user);
        refreshTokenRepository.delete(token);
    }

    private TokenResponse generateTokens(User user) {
        String refreshToken = jwtUtils.generateRefreshToken(user.getLogin());
        LocalDateTime expiresAtRefreshToken = LocalDateTime.ofInstant(
                jwtUtils.extractExpiresAt(refreshToken),
                ZoneId.systemDefault()
        );

        RefreshToken token = refreshTokenRepository.save(new RefreshToken(refreshToken, expiresAtRefreshToken));
        user.setToken(token);
        userRepository.save(user);
        String accessToken = jwtUtils.generateAccessToken(user.getLogin(), user.getId());
        return new TokenResponse(accessToken, refreshToken, jwtUtils.extractExpiresAt(accessToken).toEpochMilli());
    }

}
