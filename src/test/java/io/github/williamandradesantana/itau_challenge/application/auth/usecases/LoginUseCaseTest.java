package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.LoginCommand;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidCredentialsException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.support.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginUseCaseTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        useCase = new LoginUseCase(new FakeAuthenticator(userRepository, new FakePasswordHasher()), new FakeTokenService());
    }

    private User saveUser(String userName) {
        User user = UserFixtures.user(userName, permissionRepository.common);
        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("Test: when the credentials are valid, should return the access and refresh tokens")
    void test_WhenTheCredentialsAreValid_ShouldReturnTheAccessAndRefreshTokens() {
        saveUser("bob");

        AuthTokens tokens = useCase.execute(new LoginCommand("bob", UserFixtures.RAW_PASSWORD));

        assertEquals("access:bob:[COMMON]", tokens.accessToken());
        assertEquals("refresh:bob", tokens.refreshToken());
        assertEquals(FakeTokenService.ACCESS_TOKEN_EXPIRES_AT, tokens.accessTokenExpiresAt());
    }

    @Test
    @DisplayName("Test: the token should carry every permission of the user")
    void test_TheTokenShouldCarryEveryPermissionOfTheUser() {
        User admin = UserFixtures.user("alice", permissionRepository.admin, permissionRepository.common);
        userRepository.save(admin);

        AuthTokens tokens = useCase.execute(new LoginCommand("alice", UserFixtures.RAW_PASSWORD));

        assertEquals("access:alice:[ADMIN, COMMON]", tokens.accessToken());
    }

    @Test
    @DisplayName("Test: surrounding spaces in the user name should be ignored")
    void test_SurroundingSpacesInTheUserNameShouldBeIgnored() {
        saveUser("bob");

        AuthTokens tokens = useCase.execute(new LoginCommand("  bob  ", UserFixtures.RAW_PASSWORD));

        assertEquals("refresh:bob", tokens.refreshToken());
    }

    @Test
    @DisplayName("Test: when the command is null, should throw exception")
    void test_WhenTheCommandIsNull_ShouldThrowException() {
        assertThrows(RequiredObjectIsNullException.class, () -> useCase.execute(null));
    }

    @Test
    @DisplayName("Test: when the password is wrong, should throw invalid credentials")
    void test_WhenThePasswordIsWrong_ShouldThrowInvalidCredentials() {
        saveUser("bob");

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(new LoginCommand("bob", "wrong-password")));
    }

    @Test
    @DisplayName("Test: when the user does not exist, should throw the same error as a wrong password")
    void test_WhenTheUserDoesNotExist_ShouldThrowTheSameErrorAsAWrongPassword() {
        saveUser("bob");

        InvalidCredentialsException unknownUser = assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginCommand("ghost", UserFixtures.RAW_PASSWORD)));
        InvalidCredentialsException wrongPassword = assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginCommand("bob", "wrong-password")));

        assertEquals(wrongPassword.getMessage(), unknownUser.getMessage(), () -> "the message must not reveal whether the user exists");
    }

    @Test
    @DisplayName("Test: when the user name or the password is null or blank, should throw invalid credentials")
    void test_WhenTheUserNameOrThePasswordIsNullOrBlank_ShouldThrowInvalidCredentials() {
        saveUser("bob");

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(new LoginCommand(null, UserFixtures.RAW_PASSWORD)));
        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(new LoginCommand("  ", UserFixtures.RAW_PASSWORD)));
        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(new LoginCommand("bob", null)));
    }

    @Test
    @DisplayName("Test: when the account is disabled, locked or expired, should throw invalid credentials")
    void test_WhenTheAccountIsDisabledLockedOrExpired_ShouldThrowInvalidCredentials() {
        User disabled = saveUser("disabled");
        disabled.setEnabled(false);

        User locked = saveUser("locked");
        locked.setAccountNonLocked(false);

        User accountExpired = saveUser("accexpired");
        accountExpired.setAccountNonExpired(false);

        User credentialsExpired = saveUser("credexpired");
        credentialsExpired.setCredentialsNonExpired(false);

        for (String userName : new String[]{"disabled", "locked", "accexpired", "credexpired"}) {
            assertThrows(InvalidCredentialsException.class,
                    () -> useCase.execute(new LoginCommand(userName, UserFixtures.RAW_PASSWORD)),
                    () -> "login should have been denied for: " + userName);
        }
    }

}