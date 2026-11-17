package io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;

import java.time.Instant;

public record TokenResponse(
    boolean authenticated,
    String tokenType,
    String accessToken,
    Instant expiresAt,
    String refreshToken
) {
    public static TokenResponse from(AuthTokens tokens) {
        return new TokenResponse(
            true,
            "Bearer",
            tokens.accessToken(),
            tokens.accessTokenExpiresAt(),
            tokens.refreshToken()
        );
    }
}
