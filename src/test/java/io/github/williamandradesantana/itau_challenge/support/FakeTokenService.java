package io.github.williamandradesantana.itau_challenge.support;

import io.github.williamandradesantana.itau_challenge.application.auth.ITokenService;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidRefreshTokenException;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

public class FakeTokenService implements ITokenService {

    public static final Instant ACCESS_TOKEN_EXPIRES_AT = Instant.parse("2030-01-01T00:00:00Z");

    @Override
    public AuthTokens generateTokens(String subject, Set<String> roles) {
        return new AuthTokens(
            "access:" + subject + ":" + new TreeSet<>(roles),
            ACCESS_TOKEN_EXPIRES_AT,
            "refresh:" + subject
        );
    }

    @Override
    public String validateRefreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("refresh:")) throw new InvalidRefreshTokenException();
        return refreshToken.substring("refresh:".length());
    }
}
