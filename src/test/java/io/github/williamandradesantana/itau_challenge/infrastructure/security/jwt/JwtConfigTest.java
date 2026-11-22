package io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    private static final String SECRET = base64Of("0123456789abcdef0123456789abcdef");
    private static final String OTHER_SECRET = base64Of("ABCDEFGHIJKLMNOPQRSTUVWXYZ012345");

    private final JwtConfig config = new JwtConfig();

    private static String base64Of(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey keyFor(String secret) {
        return config.secretKey(new JwtProperties(secret, 3600, 604800));
    }

    private AuthTokens tokensFor(String secret, long accessTtl) {
        JwtProperties properties = new JwtProperties(secret, accessTtl, 604800);
        SecretKey key = config.secretKey(properties);
        return new JwtTokenService(config.jwtEncoder(key), properties, key).generateTokens("bob", Set.of("COMMON"));
    }

    @Test
    @DisplayName("Test: when the secret has thirty two bytes, should create an HS256 key")
    void test_WhenTheSecretHasThirtyTwoBytes_ShouldCreateAnHs256Key() {
        SecretKey key = keyFor(SECRET);

        assertEquals("HmacSHA256", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length);
    }

    @Test
    @DisplayName("Test: when the secret has more than thirty two bytes, should accept it")
    void test_WhenTheSecretHasMoreThanThirtyTwoBytes_ShouldAcceptIt() {
        assertDoesNotThrow(() -> keyFor(base64Of("x".repeat(48))));
    }

    @Test
    @DisplayName("Test: when the secret has less than thirty two bytes, should throw exception")
    void test_WhenTheSecretHasLessThanThirtyTwoBytes_ShouldThrowException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> keyFor(base64Of("x".repeat(31))));

        assertTrue(exception.getMessage().contains("at least 32 bytes"));
    }

    @Test
    @DisplayName("Test: when the secret is empty, should throw exception")
    void test_WhenTheSecretIsEmpty_ShouldThrowException() {
        assertThrows(IllegalStateException.class, () -> keyFor(""));
    }

    @Test
    @DisplayName("Test: when the secret is not valid base64, should throw exception")
    void test_WhenTheSecretIsNotValidBase64_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> keyFor("this is not base64!!"));
    }

    @Test
    @DisplayName("Test: the decoder should accept an access token")
    void test_TheDecoderShouldAcceptAnAccessToken() {
        JwtDecoder decoder = config.jwtDecoder(keyFor(SECRET));

        Jwt jwt = decoder.decode(tokensFor(SECRET, 3600).accessToken());

        assertEquals("bob", jwt.getSubject());
    }

    @Test
    @DisplayName("Test: the decoder should reject a refresh token, so it cannot be used as an access token")
    void test_TheDecoderShouldRejectARefreshToken() {
        JwtDecoder decoder = config.jwtDecoder(keyFor(SECRET));

        assertThrows(JwtException.class, () -> decoder.decode(tokensFor(SECRET, 3600).refreshToken()));
    }

    @Test
    @DisplayName("Test: the decoder should reject a signed token that has no token use")
    void test_TheDecoderShouldRejectASignedTokenThatHasNoTokenUse() {
        SecretKey key = keyFor(SECRET);
        JwtEncoder encoder = config.jwtEncoder(key);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("bob").issuedAt(now).expiresAt(now.plusSeconds(3600)).build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        assertThrows(JwtException.class, () -> config.jwtDecoder(key).decode(token));
    }

    @Test
    @DisplayName("Test: the decoder should reject an expired access token")
    void test_TheDecoderShouldRejectAnExpiredAccessToken() {
        SecretKey key = keyFor(SECRET);
        JwtDecoder decoder = config.jwtDecoder(key);

        Instant now = Instant.now();
        String expired = JwtTestSupport.signedToken(key, "bob", JwtClaims.ACCESS, now.minus(Duration.ofHours(2)), now.minus(Duration.ofHours(1)));

        assertThrows(JwtException.class, () -> decoder.decode(expired));
    }

    @Test
    @DisplayName("Test: the decoder should reject a token signed with another key")
    void test_TheDecoderShouldRejectATokenSignedWithAnotherKey() {
        JwtDecoder decoder = config.jwtDecoder(keyFor(SECRET));

        String foreign = tokensFor(OTHER_SECRET, 3600).accessToken();

        assertThrows(JwtException.class, () -> decoder.decode(foreign));
    }
}
