package io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String SECRET = base64Of("0123456789abcdef0123456789abcdef");
    private static final String OTHER_SECRET = base64Of("ABCDEFGHIJKLMNOPQRSTUVWXYZ012345");
    private static final long ACCESS_TTL = 3600;
    private static final long REFRESH_TTL = 604800;

    private final JwtConfig config = new JwtConfig();

    private SecretKey key;
    private JwtTokenService service;
    private JwtDecoder plainDecoder;

    private static String base64Of(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private JwtTokenService serviceWith(String secret, long accessTtl, long refreshTtl) {
        JwtProperties properties = new JwtProperties(secret, accessTtl, refreshTtl);
        SecretKey secretKey = config.secretKey(properties);
        return new JwtTokenService(config.jwtEncoder(secretKey), properties, secretKey);
    }

    @BeforeEach
    void setUp() {
        key = config.secretKey(new JwtProperties(SECRET, ACCESS_TTL, REFRESH_TTL));
        service = serviceWith(SECRET, ACCESS_TTL, REFRESH_TTL);
        plainDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Test
    @DisplayName("Test: the access token should carry the subject, the roles and the access use")
    void test_TheAccessTokenShouldCarryTheSubjectTheRolesAndTheAccessUse() {
        AuthTokens tokens = service.generateTokens("bob", Set.of("COMMON", "ADMIN"));

        Jwt jwt = plainDecoder.decode(tokens.accessToken());

        assertEquals("bob", jwt.getSubject());
        assertEquals(Set.of("COMMON", "ADMIN"), Set.copyOf(jwt.getClaimAsStringList(JwtClaims.ROLES)));
        assertEquals(JwtClaims.ACCESS, jwt.getClaimAsString(JwtClaims.TOKEN_USE));
    }

    @Test
    @DisplayName("Test: the access token should expire after the configured time and the response should match it")
    void test_TheAccessTokenShouldExpireAfterTheConfiguredTime() {
        AuthTokens tokens = service.generateTokens("bob", Set.of("COMMON"));

        Jwt jwt = plainDecoder.decode(tokens.accessToken());

        assertEquals(Duration.ofSeconds(ACCESS_TTL), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        assertEquals(jwt.getExpiresAt(), tokens.accessTokenExpiresAt(), () -> "the returned expiration must equal the token exp claim");
    }

    @Test
    @DisplayName("Test: the refresh token should carry the subject and the refresh use, but no roles")
    void test_TheRefreshTokenShouldCarryTheSubjectAndTheRefreshUseButNoRoles() {
        AuthTokens tokens = service.generateTokens("bob", Set.of("ADMIN"));

        Jwt jwt = plainDecoder.decode(tokens.refreshToken());

        assertEquals("bob", jwt.getSubject());
        assertEquals(JwtClaims.REFRESH, jwt.getClaimAsString(JwtClaims.TOKEN_USE));
        assertFalse(jwt.hasClaim(JwtClaims.ROLES), () -> "the refresh token must not carry roles");
    }

    @Test
    @DisplayName("Test: the refresh token should expire after the configured refresh time")
    void test_TheRefreshTokenShouldExpireAfterTheConfiguredRefreshTime() {
        AuthTokens tokens = service.generateTokens("bob", Set.of("COMMON"));

        Jwt jwt = plainDecoder.decode(tokens.refreshToken());

        assertEquals(Duration.ofSeconds(REFRESH_TTL), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
    }

    @Test
    @DisplayName("Test: both tokens should be signed with HS256")
    void test_BothTokensShouldBeSignedWithHs256() {
        AuthTokens tokens = service.generateTokens("bob", Set.of("COMMON"));

        assertEquals("HS256", plainDecoder.decode(tokens.accessToken()).getHeaders().get("alg"));
        assertEquals("HS256", plainDecoder.decode(tokens.refreshToken()).getHeaders().get("alg"));
    }

    @Test
    @DisplayName("Test: every token should have its own id")
    void test_EveryTokenShouldHaveItsOwnId() {
        AuthTokens first = service.generateTokens("bob", Set.of("COMMON"));
        AuthTokens second = service.generateTokens("bob", Set.of("COMMON"));

        Set<String> ids = new HashSet<>(List.of(
                plainDecoder.decode(first.accessToken()).getId(),
                plainDecoder.decode(first.refreshToken()).getId(),
                plainDecoder.decode(second.accessToken()).getId(),
                plainDecoder.decode(second.refreshToken()).getId()));

        assertEquals(4, ids.size());
    }

    @Test
    @DisplayName("Test: validateRefreshToken should return the subject of a valid refresh token")
    void test_ValidateRefreshTokenShouldReturnTheSubjectOfAValidRefreshToken() {
        AuthTokens tokens = service.generateTokens("alice", Set.of("ADMIN"));

        assertEquals("alice", service.validateRefreshToken(tokens.refreshToken()));
    }

    @Test
    @DisplayName("Test: validateRefreshToken should reject an access token")
    void test_ValidateRefreshTokenShouldRejectAnAccessToken() {
        AuthTokens tokens = service.generateTokens("bob", Set.of("COMMON"));

        assertThrows(InvalidRefreshTokenException.class, () -> service.validateRefreshToken(tokens.accessToken()));
    }

    @Test
    @DisplayName("Test: validateRefreshToken should reject malformed tokens")
    void test_ValidateRefreshTokenShouldRejectMalformedTokens() {
        for (String malformed : new String[]{"garbage", "", "a.b.c", "only.two"}) {
            assertThrows(InvalidRefreshTokenException.class,
                    () -> service.validateRefreshToken(malformed),
                    () -> "should have rejected: '" + malformed + "'");
        }
    }

    @Test
    @DisplayName("Test: validateRefreshToken should reject a token whose signature was tampered with")
    void test_ValidateRefreshTokenShouldRejectATokenWhoseSignatureWasTamperedWith() {
        String token = service.generateTokens("bob", Set.of("COMMON")).refreshToken();
        int signatureStart = token.lastIndexOf('.') + 1;
        char replacement = token.charAt(signatureStart) == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, signatureStart) + replacement + token.substring(signatureStart + 1);

        assertThrows(InvalidRefreshTokenException.class, () -> service.validateRefreshToken(tampered));
    }

    @Test
    @DisplayName("Test: validateRefreshToken should reject a token signed with another key")
    void test_ValidateRefreshTokenShouldRejectATokenSignedWithAnotherKey() {
        String foreign = serviceWith(OTHER_SECRET, ACCESS_TTL, REFRESH_TTL).generateTokens("bob", Set.of("COMMON")).refreshToken();

        assertThrows(InvalidRefreshTokenException.class, () -> service.validateRefreshToken(foreign));
    }

    @Test
    @DisplayName("Test: validateRefreshToken should reject an expired refresh token")
    void test_ValidateRefreshTokenShouldRejectAnExpiredRefreshToken() {
        Instant now = Instant.now();
        String expired = JwtTestSupport.signedToken(key, "bob", JwtClaims.REFRESH, now.minus(Duration.ofHours(2)), now.minus(Duration.ofHours(1)));

        assertThrows(InvalidRefreshTokenException.class, () -> service.validateRefreshToken(expired));
    }

    @Test
    @DisplayName("Test: validateRefreshToken should reject an unsigned token")
    void test_ValidateRefreshTokenShouldRejectAnUnsignedToken() {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = encoder.encodeToString(
                "{\"sub\":\"bob\",\"token_use\":\"refresh\",\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8));
        String unsigned = header + "." + payload + ".";

        assertThrows(InvalidRefreshTokenException.class, () -> service.validateRefreshToken(unsigned));
    }

    @Test
    @DisplayName("Test: validateRefreshToken should reject a signed refresh token that has no subject")
    void test_ValidateRefreshTokenShouldRejectASignedRefreshTokenThatHasNoSubject() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now).expiresAt(now.plusSeconds(3600)).claim(JwtClaims.TOKEN_USE, JwtClaims.REFRESH).build();
        String withoutSubject = config.jwtEncoder(key)
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        assertThrows(InvalidRefreshTokenException.class, () -> service.validateRefreshToken(withoutSubject));
    }
}
