package io.github.williamandradesantana.itau_challenge.interfaces;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt.JwtClaims;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityRulesIntegrationTest extends ApiIntegrationTest {

    private static final String USERS = "/api/users/v1";
    private static final String ME = USERS + "/me";
    private static final String AUTHORIZATION = "Authorization";
    private static final String ALLOWED_ORIGIN = "http://localhost:3000";

    @Autowired
    private JwtEncoder jwtEncoder;

    private static final JwtEncoder FOREIGN_ENCODER = new NimbusJwtEncoder(new ImmutableSecret<>(
            new SecretKeySpec("ABCDEFGHIJKLMNOPQRSTUVWXYZ012345".getBytes(StandardCharsets.UTF_8), "HmacSHA256")));

    private String signed(JwtEncoder encoder, String subject, String tokenUse, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().subject(subject).issuedAt(issuedAt).expiresAt(expiresAt);
        if (tokenUse != null) claims.claim(JwtClaims.TOKEN_USE, tokenUse);
        claims.claim(JwtClaims.ROLES, List.of("ADMIN"));
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
    }

    private String adminPayloadJson(String userName) {
        return "{\"sub\":\"" + userName + "\",\"roles\":[\"ADMIN\"],\"token_use\":\"access\",\"iat\":1,\"exp\":9999999999}";
    }

    private String base64Url(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Test: the auth routes should be public: without a token they reach validation instead of answering 401")
    void test_TheAuthRoutesShouldBePublic() throws Exception {
        for (String url : new String[]{"/auth/login", "/auth/register", "/auth/refresh"}) {
            postJson(url, "{}").andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Test: a protected route without a token should answer 401 asking for a bearer token")
    void test_AProtectedRouteWithoutAToken_ShouldAnswer401AskingForABearerToken() throws Exception {
        MvcResult result = mockMvc.perform(get(ME)).andExpect(status().isUnauthorized()).andReturn();

        String challenge = result.getResponse().getHeader("WWW-Authenticate");
        assertNotNull(challenge, () -> "a 401 must carry the WWW-Authenticate header");
        assertTrue(challenge.startsWith("Bearer"), () -> "challenge was: " + challenge);
    }

    @Test
    @DisplayName("Test: malformed authorization headers should answer 401")
    void test_MalformedAuthorizationHeaders_ShouldAnswer401() throws Exception {
        for (String header : new String[]{"Bearer", "Bearer garbage", "Bearer a.b.c", "Basic dXNlcjpwYXNz", "just-a-token"}) {
            mockMvc.perform(get(ME).header(AUTHORIZATION, header))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("Test: a refresh token used as bearer should answer 401")
    void test_ARefreshTokenUsedAsBearer_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(refreshTokenOf(userName))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: an expired access token should answer 401")
    void test_AnExpiredAccessToken_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");
        Instant now = Instant.now();
        String expired = signed(jwtEncoder, userName, JwtClaims.ACCESS, now.minus(Duration.ofHours(2)), now.minus(Duration.ofHours(1)));

        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(expired))).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: a token signed with another key should answer 401")
    void test_ATokenSignedWithAnotherKey_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");
        Instant now = Instant.now();
        String foreign = signed(FOREIGN_ENCODER, userName, JwtClaims.ACCESS, now, now.plus(Duration.ofHours(1)));

        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(foreign))).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: a correctly signed token that has no token use should answer 401")
    void test_ACorrectlySignedTokenThatHasNoTokenUse_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");
        Instant now = Instant.now();
        String withoutUse = signed(jwtEncoder, userName, null, now, now.plus(Duration.ofHours(1)));

        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(withoutUse))).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: a token whose payload was changed to claim the admin role should answer 401, not 200")
    void test_ATokenWhosePayloadWasChangedToClaimTheAdminRole_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");
        String[] parts = accessTokenOf(userName).split("\\.");
        String forged = parts[0] + "." + base64Url(adminPayloadJson(userName)) + "." + parts[2];

        mockMvc.perform(get(USERS).header(AUTHORIZATION, bearer(accessTokenOf(userName))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(USERS).header(AUTHORIZATION, bearer(forged)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: an unsigned token claiming the admin role should answer 401")
    void test_AnUnsignedTokenClaimingTheAdminRole_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");
        String unsigned = base64Url("{\"alg\":\"none\"}") + "." + base64Url(adminPayloadJson(userName)) + ".";

        mockMvc.perform(get(USERS).header(AUTHORIZATION, bearer(unsigned))).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: a valid token of a user that was deleted still authenticates, but the user is not found")
    void test_AValidTokenOfAUserThatWasDeleted_StillAuthenticatesButTheUserIsNotFound() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");
        String token = accessTokenOf(userName);

        userRepository.delete(user.getId());

        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Test: an unknown route should answer 404 with a json body for an authenticated caller and 401 otherwise")
    void test_AnUnknownRoute_ShouldAnswer404WithAJsonBodyForAnAuthenticatedCallerAnd401Otherwise() throws Exception {
        mockMvc.perform(get("/this/route/does/not/exist").header(AUTHORIZATION, bearer(commonToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
        mockMvc.perform(get("/this/route/does/not/exist")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: a wrong http method should answer 405 and not 500")
    void test_AWrongHttpMethod_ShouldAnswer405AndNot500() throws Exception {
        mockMvc.perform(put("/auth/login").header(AUTHORIZATION, bearer(commonToken())))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Test: the api should be stateless and never set a session cookie")
    void test_TheApiShouldBeStatelessAndNeverSetASessionCookie() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        login(userName, PASSWORD).andExpect(header().doesNotExist("Set-Cookie"));
        mockMvc.perform(get(ME).header(AUTHORIZATION, bearer(accessTokenOf(userName))))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    @DisplayName("Test: state changing requests should not require a csrf token")
    void test_StateChangingRequestsShouldNotRequireACsrfToken() throws Exception {
        mockMvc.perform(delete(USERS + "/" + UUID.randomUUID()).header(AUTHORIZATION, bearer(adminToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test: a preflight request from an allowed origin should be accepted")
    void test_APreflightRequestFromAnAllowedOrigin_ShouldBeAccepted() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("Test: a preflight request from an unknown origin should be rejected")
    void test_APreflightRequestFromAnUnknownOrigin_ShouldBeRejected() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
