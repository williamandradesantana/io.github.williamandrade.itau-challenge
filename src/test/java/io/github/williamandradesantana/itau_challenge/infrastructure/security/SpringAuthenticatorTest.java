package io.github.williamandradesantana.itau_challenge.infrastructure.security;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthenticatedUser;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidCredentialsException;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpringAuthenticatorTest {

    private static final String PASSWORD = "Senha@123";

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private SpringAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(new DomainUserDetailsService(userRepository));
        provider.setPasswordEncoder(encoder);
        authenticator = new SpringAuthenticator(new ProviderManager(provider));
    }

    private User saveUser(String userName, Permission... permissions) {
        User user = User.create(userName, "Full " + userName, encoder.encode(PASSWORD), Set.of(permissions));
        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("Test: when the credentials are valid, should return the user name and the roles")
    void test_WhenTheCredentialsAreValid_ShouldReturnTheUserNameAndTheRoles() {
        saveUser("bob", permissionRepository.common);

        AuthenticatedUser result = authenticator.authenticate("bob", PASSWORD);

        assertEquals("bob", result.userName());
        assertEquals(Set.of("COMMON"), result.roles());
    }

    @Test
    @DisplayName("Test: the roles should be exactly the user permissions, with nothing added by the framework")
    void test_TheRolesShouldBeExactlyTheUserPermissions() {
        saveUser("alice", permissionRepository.admin, permissionRepository.common);

        AuthenticatedUser result = authenticator.authenticate("alice", PASSWORD);

        assertEquals(Set.of("ADMIN", "COMMON"), result.roles());
        assertTrue(result.roles().stream().noneMatch(role -> role.startsWith("FACTOR_")));
    }

    @Test
    @DisplayName("Test: surrounding spaces in the user name should be ignored")
    void test_SurroundingSpacesInTheUserNameShouldBeIgnored() {
        saveUser("bob", permissionRepository.common);

        assertEquals("bob", authenticator.authenticate("  bob  ", PASSWORD).userName());
    }

    @Test
    @DisplayName("Test: when the password is wrong, should throw invalid credentials")
    void test_WhenThePasswordIsWrong_ShouldThrowInvalidCredentials() {
        saveUser("bob", permissionRepository.common);

        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate("bob", "wrong-password"));
        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate("bob", ""));
    }

    @Test
    @DisplayName("Test: when the user does not exist, should fail exactly like a wrong password")
    void test_WhenTheUserDoesNotExist_ShouldFailExactlyLikeAWrongPassword() {
        saveUser("bob", permissionRepository.common);

        InvalidCredentialsException unknownUser = assertThrows(InvalidCredentialsException.class,
            () -> authenticator.authenticate("ghost", PASSWORD));
        InvalidCredentialsException wrongPassword = assertThrows(InvalidCredentialsException.class,
            () -> authenticator.authenticate("bob", "wrong-password"));

        assertEquals(wrongPassword.getMessage(), unknownUser.getMessage(), () -> "the message must not reveal whether the user exists");
    }

    @Test
    @DisplayName("Test: when the account is disabled, locked or expired, should throw invalid credentials")
    void test_WhenTheAccountIsDisabledLockedOrExpired_ShouldThrowInvalidCredentials() {
        saveUser("disabled", permissionRepository.common).setEnabled(false);
        saveUser("locked", permissionRepository.common).setAccountNonLocked(false);
        saveUser("accexpired", permissionRepository.common).setAccountNonExpired(false);
        saveUser("credexpired", permissionRepository.common).setCredentialsNonExpired(false);

        for (String userName : new String[]{"disabled", "locked", "accexpired", "credexpired"}) {
            assertThrows(InvalidCredentialsException.class,
                () -> authenticator.authenticate(userName, PASSWORD),
                () -> "login should have been denied for: " + userName);
        }
    }

    @Test
    @DisplayName("Test: when the user name or the password is null or blank, should not even call the authentication manager")
    void test_WhenTheUserNameOrThePasswordIsNullOrBlank_ShouldNotEvenCallTheAuthenticationManager() {
        AuthenticationManager mustNotBeCalled = authentication -> {
            throw new AssertionError("the authentication manager must not be called for empty input");
        };
        SpringAuthenticator strictAuthenticator = new SpringAuthenticator(mustNotBeCalled);

        assertThrows(InvalidCredentialsException.class, () -> strictAuthenticator.authenticate(null, PASSWORD));
        assertThrows(InvalidCredentialsException.class, () -> strictAuthenticator.authenticate("", PASSWORD));
        assertThrows(InvalidCredentialsException.class, () -> strictAuthenticator.authenticate("   ", PASSWORD));
        assertThrows(InvalidCredentialsException.class, () -> strictAuthenticator.authenticate("bob", null));
    }
}
