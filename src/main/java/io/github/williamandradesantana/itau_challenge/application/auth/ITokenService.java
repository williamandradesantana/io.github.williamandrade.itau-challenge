package io.github.williamandradesantana.itau_challenge.application.auth;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;

import java.util.Set;

public interface ITokenService {
    AuthTokens generateTokens(String subject, Set<String> roles);
    String validateRefreshToken(String refreshToken);
}
