package io.github.williamandradesantana.itau_challenge.application.auth.dtos;

import java.time.Instant;

public record AuthTokens(String accessToken, Instant accessTokenExpiresAt, String refreshToken) {}
