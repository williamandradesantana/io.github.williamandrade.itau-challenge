package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.RefreshTokenCommand;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidRefreshTokenException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.support.FakeTokenService;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.InMemoryUserRepository;
import io.github.williamandradesantana.itau_challenge.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenUseCaseTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        useCase = new RefreshTokenUseCase(userRepository, new FakeTokenService());
    }

    private User saveUser(String userName) {
        User user = UserFixtures.user(userName, permissionRepository.common);
        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("Test: when the refresh token is valid, should return a new pair of tokens")
    void test_WhenTheRefreshTokenIsValid_ShouldReturnANewPairOfTokens() {
        saveUser("bob");

        AuthTokens tokens = useCase.execute(new RefreshTokenCommand("refresh:bob"));

        assertEquals("access:bob:[COMMON]", tokens.accessToken());
        assertEquals("refresh:bob", tokens.refreshToken());
        assertEquals(FakeTokenService.ACCESS_TOKEN_EXPIRES_AT, tokens.accessTokenExpiresAt());
    }

    @Test
    @DisplayName("Test: the new access token should reflect the permissions the user has now")
    void test_TheNewAccessTokenShouldReflectThePermissionsTheUserHasNow() {
        User bob = saveUser("bob");
        bob.grantPermission(permissionRepository.admin);

        AuthTokens tokens = useCase.execute(new RefreshTokenCommand("refresh:bob"));

        assertEquals("access:bob:[ADMIN, COMMON]", tokens.accessToken());
    }

    @Test
    @DisplayName("Test: when the command is null, should throw exception")
    void test_WhenTheCommandIsNull_ShouldThrowException() {
        assertThrows(RequiredObjectIsNullException.class, () -> useCase.execute(null));
    }

    @Test
    @DisplayName("Test: when the refresh token is null or blank, should throw exception")
    void test_WhenTheRefreshTokenIsNullOrBlank_ShouldThrowException() {
        saveUser("bob");

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute(new RefreshTokenCommand(null)));
        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute(new RefreshTokenCommand("")));
        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute(new RefreshTokenCommand("   ")));
    }

    @Test
    @DisplayName("Test: when the token is not a valid refresh token, should throw exception")
    void test_WhenTheTokenIsNotAValidRefreshToken_ShouldThrowException() {
        saveUser("bob");

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute(new RefreshTokenCommand("access:bob:[COMMON]")));
        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute(new RefreshTokenCommand("garbage")));
    }

    @Test
    @DisplayName("Test: when the user no longer exists, should throw exception")
    void test_WhenTheUserNoLongerExists_ShouldThrowException() {
        User bob = saveUser("bob");
        userRepository.delete(bob.getId());

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute(new RefreshTokenCommand("refresh:bob")));
    }

    @Test
    @DisplayName("Test: when the user was disabled, locked or expired after the login, should throw exception")
    void test_WhenTheUserWasDisabledLockedOrExpiredAfterTheLogin_ShouldThrowException() {
        User disabled = saveUser("disabled");
        disabled.setEnabled(false);

        User locked = saveUser("locked");
        locked.setAccountNonLocked(false);

        User accountExpired = saveUser("accexpired");
        accountExpired.setAccountNonExpired(false);

        User credentialsExpired = saveUser("credexpired");
        credentialsExpired.setCredentialsNonExpired(false);

        for (String userName : new String[]{"disabled", "locked", "accexpired", "credexpired"}) {
            assertThrows(InvalidRefreshTokenException.class,
                    () -> useCase.execute(new RefreshTokenCommand("refresh:" + userName)),
                    () -> "refresh should have been denied for: " + userName);
        }
    }

}