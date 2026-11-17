package io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt;

import io.github.williamandradesantana.itau_challenge.application.auth.ITokenService;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidRefreshTokenException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtTokenService implements ITokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final JwtDecoder refreshTokenDecoder;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties properties, SecretKey secretKey) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), new TokenUseValidator(JwtClaims.REFRESH)));

        this.refreshTokenDecoder = decoder;

    }

    @Override
    public AuthTokens generateTokens(String subject, Set<String> roles) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Jwt accessToken = encode(
            JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(properties.expirationInSeconds()))
                .claim(JwtClaims.TOKEN_USE, JwtClaims.ACCESS)
                .claim(JwtClaims.ROLES, roles)
            .build()
        );

        Jwt refreshToken = encode(
            JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(properties.refreshExpirationInSeconds()))
                .claim(JwtClaims.TOKEN_USE, JwtClaims.REFRESH)
            .build()
        );
        return new AuthTokens(accessToken.getTokenValue(), accessToken.getExpiresAt(), refreshToken.getTokenValue());
    }

    @Override
    public String validateRefreshToken(String refreshToken) {
        try {
            String subject = refreshTokenDecoder.decode(refreshToken).getSubject();
            if (subject == null || subject.isBlank()) throw new InvalidRefreshTokenException();
            return subject;
        } catch (JwtException ex) {
            throw new InvalidRefreshTokenException();
        }
    }

    private Jwt encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
    }
}
