package ru.edu.retro.apiservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtUtils {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;
    public static final long EXPIRATION_TIME_ACCESS_TOKEN = 1000 * 60 * 60; // 1 час
    public static final long EXPIRATION_TIME_REFRESH_TOKEN = 1000 * 60 * 60 * 24 * 7; // 7 дней

    public Optional<String> extractJwt(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return Optional.of(authorizationHeader.substring(7));
        }
        return Optional.empty();
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String extractLogin(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateAccessToken(String login, Long id) {
        return Jwts.builder()
                .subject(login)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS_TOKEN))
                .signWith(getSignInKey())
                .claims(Collections.singletonMap("id", id))
                .compact();
    }

    public String generateRefreshToken(String login) {
        return Jwts.builder()
                .subject(login)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_REFRESH_TOKEN))
                .signWith(getSignInKey())
                .compact();
    }

    public Instant extractExpiresAt(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }
}