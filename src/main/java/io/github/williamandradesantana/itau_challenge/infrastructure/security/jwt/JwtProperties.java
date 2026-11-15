package io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, long expirationInSeconds) {
}
