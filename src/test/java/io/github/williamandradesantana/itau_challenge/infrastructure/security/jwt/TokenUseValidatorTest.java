package io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;

class TokenUseValidatorTest {

    private Jwt jwtWithUse(String tokenUse) {
        return Jwt.withTokenValue("token").header("alg", "HS256").claim(JwtClaims.TOKEN_USE, tokenUse).build();
    }

    @Test
    @DisplayName("Test: when the token use is the expected one, should succeed")
    void test_WhenTheTokenUseIsTheExpectedOne_ShouldSucceed() {
        assertFalse(new TokenUseValidator(JwtClaims.ACCESS).validate(jwtWithUse(JwtClaims.ACCESS)).hasErrors());
        assertFalse(new TokenUseValidator(JwtClaims.REFRESH).validate(jwtWithUse(JwtClaims.REFRESH)).hasErrors());
    }

    @Test
    @DisplayName("Test: when the token use is the other one, should fail with invalid token")
    void test_WhenTheTokenUseIsTheOtherOne_ShouldFailWithInvalidToken() {
        OAuth2TokenValidatorResult refreshAsAccess = new TokenUseValidator(JwtClaims.ACCESS).validate(jwtWithUse(JwtClaims.REFRESH));
        OAuth2TokenValidatorResult accessAsRefresh = new TokenUseValidator(JwtClaims.REFRESH).validate(jwtWithUse(JwtClaims.ACCESS));

        assertTrue(refreshAsAccess.hasErrors());
        assertTrue(accessAsRefresh.hasErrors());
        assertEquals("invalid_token", refreshAsAccess.getErrors().iterator().next().getErrorCode());
    }

    @Test
    @DisplayName("Test: when the token has no use claim, should fail")
    void test_WhenTheTokenHasNoUseClaim_ShouldFail() {
        Jwt withoutUse = Jwt.withTokenValue("token").header("alg", "HS256").claim("sub", "bob").build();

        assertTrue(new TokenUseValidator(JwtClaims.ACCESS).validate(withoutUse).hasErrors());
    }

    @Test
    @DisplayName("Test: when the token use is unknown, should fail")
    void test_WhenTheTokenUseIsUnknown_ShouldFail() {
        assertTrue(new TokenUseValidator(JwtClaims.ACCESS).validate(jwtWithUse("something-else")).hasErrors());
    }
}
