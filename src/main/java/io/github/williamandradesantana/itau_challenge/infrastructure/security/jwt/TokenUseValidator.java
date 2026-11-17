package io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class TokenUseValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedUse;

    public TokenUseValidator(String expectedUse) {
        this.expectedUse = expectedUse;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (expectedUse.equals(jwt.getClaimAsString(JwtClaims.TOKEN_USE))) return OAuth2TokenValidatorResult.success();
        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Only" + expectedUse + " tokens are accepted here", null)
        );
    }
}
