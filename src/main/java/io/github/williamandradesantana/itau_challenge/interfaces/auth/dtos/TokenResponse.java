package io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos;

public record TokenResponse(String accessToken, String tokenType) {
    public static TokenResponse bearer(String accessToken) {
        return new TokenResponse(accessToken, "Bearer");
    }
}
