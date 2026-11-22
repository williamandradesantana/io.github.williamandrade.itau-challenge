package io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import javax.crypto.SecretKey;
import java.time.Instant;

final class JwtTestSupport {

    private JwtTestSupport() {}

    static String signedToken(SecretKey key, String subject, String tokenUse, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(JwtClaims.TOKEN_USE, tokenUse)
                .build();

        return new JwtConfig().jwtEncoder(key)
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}

