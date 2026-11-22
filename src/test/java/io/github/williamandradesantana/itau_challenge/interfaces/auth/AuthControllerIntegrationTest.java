package io.github.williamandradesantana.itau_challenge.interfaces.auth;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.interfaces.ApiIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends ApiIntegrationTest {

    private static final String ME = "/api/users/v1/me";

    // ============================ POST /auth/register ============================

    @Test
    @DisplayName("Test: register with valid data should create the user and answer 201 without a body")
    void test_RegisterWithValidData_ShouldCreateTheUserAndAnswer201WithoutABody() throws Exception {
        String userName = uniqueName("bob");

        register(userName, "Bob Silva", PASSWORD)
            .andExpect(status().isCreated())
            .andExpect(content().string(""));

        assertTrue(userRepository.findByUserName(userName).isPresent());
        login(userName, PASSWORD).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test: register should trim the user name")
    void test_RegisterShouldTrimTheUserName() throws Exception {
        String userName = uniqueName("bob");

        register("  " + userName + "  ", "Bob Silva", PASSWORD).andExpect(status().isCreated());

        login(userName, PASSWORD).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test: register with a user name already in use should answer 409")
    void test_RegisterWithAUserNameAlreadyInUse_ShouldAnswer409() throws Exception {
        String userName = uniqueName("bob");
        register(userName, "Bob Silva", PASSWORD).andExpect(status().isCreated());

        ResultActions second = register(userName, "Other Bob", PASSWORD).andExpect(status().isConflict());

        assertEquals("UserName already exists: " + userName, messageOf(second));
    }

    @Test
    @DisplayName("Test: register with a user name that differs only by spaces should answer 409")
    void test_RegisterWithAUserNameThatDiffersOnlyBySpaces_ShouldAnswer409() throws Exception {
        String userName = uniqueName("bob");
        register(userName, "Bob Silva", PASSWORD).andExpect(status().isCreated());

        register("  " + userName + "  ", "Other Bob", PASSWORD).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Test: register with a short password should answer 400")
    void test_RegisterWithAShortPassword_ShouldAnswer400() throws Exception {
        ResultActions result = register(uniqueName("bob"), "Bob Silva", "1234567").andExpect(status().isBadRequest());

        assertTrue(messageOf(result).contains("at least 8"));
    }

    @Test
    @DisplayName("Test: register with a too short user name should answer 400 and save nothing")
    void test_RegisterWithATooShortUserName_ShouldAnswer400AndSaveNothing() throws Exception {
        register("ab", "Bob Silva", PASSWORD).andExpect(status().isBadRequest());

        assertTrue(userRepository.findByUserName("ab").isEmpty());
    }

    @Test
    @DisplayName("Test: register with blank fields should answer 400 naming every invalid field")
    void test_RegisterWithBlankFields_ShouldAnswer400NamingEveryInvalidField() throws Exception {
        ResultActions result = register("", "", "").andExpect(status().isBadRequest());

        String message = messageOf(result);
        assertTrue(message.contains("userName"), () -> "message was: " + message);
        assertTrue(message.contains("fullName"), () -> "message was: " + message);
        assertTrue(message.contains("password"), () -> "message was: " + message);
    }

    @Test
    @DisplayName("Test: register with missing fields should answer 400")
    void test_RegisterWithMissingFields_ShouldAnswer400() throws Exception {
        ResultActions result = postJson("/auth/register", "{}").andExpect(status().isBadRequest());

        assertTrue(messageOf(result).contains("userName"));
    }

    @Test
    @DisplayName("Test: register with a malformed or missing body should answer 400 with a generic message")
    void test_RegisterWithAMalformedOrMissingBody_ShouldAnswer400WithAGenericMessage() throws Exception {
        ResultActions malformed = postJson("/auth/register", "{not json").andExpect(status().isBadRequest());
        ResultActions empty = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals("Malformed or missing request body", messageOf(malformed));
        assertEquals("Malformed or missing request body", messageOf(empty));
    }

    @Test
    @DisplayName("Test: register with a content type that is not json should answer 415")
    void test_RegisterWithAContentTypeThatIsNotJson_ShouldAnswer415() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.TEXT_PLAIN).content("userName=bob"))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Test: register should ignore permissions sent by the client and always create a common user")
    void test_RegisterShouldIgnorePermissionsSentByTheClient() throws Exception {
        String userName = uniqueName("evil");

        postJson("/auth/register", """
                {"userName":"%s","fullName":"Evil User","password":"%s","permissions":["ADMIN"],"roles":["ADMIN"],"enabled":true}"""
                .formatted(userName, PASSWORD)).andExpect(status().isCreated());

        mockMvc.perform(get(ME).header("Authorization", bearer(accessTokenOf(userName))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles[0]").value("COMMON"))
            .andExpect(jsonPath("$.roles[1]").doesNotExist());
    }

    // ============================ POST /auth/login ============================

    @Test
    @DisplayName("Test: login with valid credentials should answer 200 with both tokens, the expiration and authenticated true")
    void test_LoginWithValidCredentials_ShouldAnswer200WithBothTokens() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        login(userName, PASSWORD)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.expiresAt").isNotEmpty())
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Test: login should give different access and refresh tokens")
    void test_LoginShouldGiveDifferentAccessAndRefreshTokens() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        String body = loginBody(userName);

        assertNotEquals(JsonPath.<String>read(body, "$.accessToken"), JsonPath.<String>read(body, "$.refreshToken"));
    }

    @Test
    @DisplayName("Test: the access token from the login should open a protected route")
    void test_TheAccessTokenFromTheLogin_ShouldOpenAProtectedRoute() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        mockMvc.perform(get(ME).header("Authorization", bearer(accessTokenOf(userName))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value(userName));
    }

    @Test
    @DisplayName("Test: login should ignore spaces around the user name")
    void test_LoginShouldIgnoreSpacesAroundTheUserName() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        login("  " + userName + "  ", PASSWORD).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test: login with a wrong password should answer 401")
    void test_LoginWithAWrongPassword_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        ResultActions result = login(userName, "wrong-password").andExpect(status().isUnauthorized());

        assertEquals("Invalid username or password", messageOf(result));
    }

    @Test
    @DisplayName("Test: login with an unknown user should answer exactly like a wrong password")
    void test_LoginWithAnUnknownUser_ShouldAnswerExactlyLikeAWrongPassword() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        ResultActions unknown = login(uniqueName("ghost"), PASSWORD).andExpect(status().isUnauthorized());
        ResultActions wrongPassword = login(userName, "wrong-password").andExpect(status().isUnauthorized());

        assertEquals(messageOf(wrongPassword), messageOf(unknown), () -> "the message must not reveal whether the user exists");
    }

    @Test
    @DisplayName("Test: login of a disabled user should answer 401 even with the right password")
    void test_LoginOfADisabledUser_ShouldAnswer401EvenWithTheRightPassword() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, false, "COMMON");

        login(userName, PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: login with blank fields should answer 400 naming the fields")
    void test_LoginWithBlankFields_ShouldAnswer400NamingTheFields() throws Exception {
        ResultActions result = login("", "").andExpect(status().isBadRequest());

        String message = messageOf(result);
        assertTrue(message.contains("userName"), () -> "message was: " + message);
        assertTrue(message.contains("password"), () -> "message was: " + message);
    }

    @Test
    @DisplayName("Test: login with a malformed body should answer 400")
    void test_LoginWithAMalformedBody_ShouldAnswer400() throws Exception {
        postJson("/auth/login", "{not json").andExpect(status().isBadRequest());
    }

    // ============================ POST /auth/refresh ============================

    @Test
    @DisplayName("Test: refresh with a valid refresh token should answer 200 with a new pair that works")
    void test_RefreshWithAValidRefreshToken_ShouldAnswer200WithANewPairThatWorks() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        String body = refresh(refreshTokenOf(userName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.expiresAt").isNotEmpty())
            .andReturn().getResponse().getContentAsString();

        String newAccessToken = JsonPath.read(body, "$.accessToken");
        mockMvc.perform(get(ME).header("Authorization", bearer(newAccessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value(userName));
    }

    @Test
    @DisplayName("Test: refresh with an access token should answer 401")
    void test_RefreshWithAnAccessToken_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        createUser(userName, "COMMON");

        ResultActions result = refresh(accessTokenOf(userName)).andExpect(status().isUnauthorized());

        assertEquals("Invalid or expired refresh token", messageOf(result));
    }

    @Test
    @DisplayName("Test: refresh with a garbage token should answer 401")
    void test_RefreshWithAGarbageToken_ShouldAnswer401() throws Exception {
        refresh("this.is.not-a-token").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: refresh with a blank token should answer 400 naming the field")
    void test_RefreshWithABlankToken_ShouldAnswer400NamingTheField() throws Exception {
        ResultActions result = refresh("").andExpect(status().isBadRequest());

        assertTrue(messageOf(result).contains("refreshToken"));
    }

    @Test
    @DisplayName("Test: refresh after the user was disabled should answer 401")
    void test_RefreshAfterTheUserWasDisabled_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");
        String refreshToken = refreshTokenOf(userName);

        user.setEnabled(false);
        userRepository.save(user);

        refresh(refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: refresh after the user was deleted should answer 401")
    void test_RefreshAfterTheUserWasDeleted_ShouldAnswer401() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");
        String refreshToken = refreshTokenOf(userName);

        userRepository.delete(user.getId());

        refresh(refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test: the new access token should reflect the permissions the user has at refresh time")
    void test_TheNewAccessTokenShouldReflectThePermissionsTheUserHasAtRefreshTime() throws Exception {
        String userName = uniqueName("bob");
        User user = createUser(userName, "COMMON");
        String body = loginBody(userName);
        String oldAccessToken = JsonPath.read(body, "$.accessToken");
        String refreshToken = JsonPath.read(body, "$.refreshToken");

        user.grantPermission(permissionRepository.findByPermissionName("ADMIN").orElseThrow());
        userRepository.save(user);

        mockMvc.perform(get("/api/users/v1").header("Authorization", bearer(oldAccessToken)))
            .andExpect(status().isForbidden());

        String newAccessToken = JsonPath.read(
            refresh(refreshToken).andReturn().getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(get("/api/users/v1").header("Authorization", bearer(newAccessToken)))
            .andExpect(status().isOk());
    }
}
