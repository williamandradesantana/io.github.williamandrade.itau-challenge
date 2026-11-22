package io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TokenResponseTest {

    private static final Instant EXPIRES_AT = Instant.parse("2030-01-01T12:00:00Z");

    @Test
    @DisplayName("Test: from should mark the response as authenticated and as a bearer token")
    void test_FromShouldMarkTheResponseAsAuthenticatedAndAsABearerToken() {
        TokenResponse response = TokenResponse.from(new AuthTokens("access-token", EXPIRES_AT, "refresh-token"));

        assertTrue(response.authenticated());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    @DisplayName("Test: from should copy the tokens and the access token expiration")
    void test_FromShouldCopyTheTokensAndTheAccessTokenExpiration() {
        TokenResponse response = TokenResponse.from(new AuthTokens("access-token", EXPIRES_AT, "refresh-token"));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(EXPIRES_AT, response.expiresAt());
    }
}
