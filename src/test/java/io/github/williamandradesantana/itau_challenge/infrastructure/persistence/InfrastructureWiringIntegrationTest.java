package io.github.williamandradesantana.itau_challenge.infrastructure.persistence;

import io.github.williamandradesantana.itau_challenge.application.auth.IAuthenticator;
import io.github.williamandradesantana.itau_challenge.application.auth.IPasswordHasher;
import io.github.williamandradesantana.itau_challenge.application.auth.ITokenService;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthenticatedUser;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidCredentialsException;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.infrastructure.security.BCryptPasswordHasher;
import io.github.williamandradesantana.itau_challenge.infrastructure.security.jwt.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InfrastructureWiringIntegrationTest extends PostgresIntegrationTest {

    private static final String PASSWORD = "Senha@123";

    @Autowired
    private ApplicationContext context;

    @Autowired
    private IPasswordHasher passwordHasher;

    @Autowired
    private IAuthenticator authenticator;

    @Autowired
    private ITokenService tokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private User saveUser(String userName) {
        User user = User.create(
            userName, "Full " + userName, passwordHasher.hash(PASSWORD),
            Set.of(permissionRepository.findByPermissionName("COMMON").orElseThrow())
        );
        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("Test: the context should have exactly one candidate for each infrastructure type")
    void test_TheContextShouldHaveExactlyOneCandidateForEachInfrastructureType() {
        List<Class<?>> types = List.of(
            UserRepository.class, PermissionRepository.class,
            IPasswordHasher.class, ITokenService.class, IAuthenticator.class,
            PasswordEncoder.class, AuthenticationManager.class,
            JwtEncoder.class, JwtDecoder.class, SecurityFilterChain.class
        );

        for (Class<?> type : types)
            assertDoesNotThrow(
                () -> context.getBean(type), () -> "expected exactly one bean of type " + type.getSimpleName()
            );
    }

    @Test
    @DisplayName("Test: the application ports should be implemented by expected adapters")
    void test_TheApplicationPortsShouldBeImplementedByTheExpectedAdapters() {
        assertInstanceOf(BCryptPasswordHasher.class, passwordHasher);
        assertInstanceOf(JwtTokenService.class, tokenService);
        assertInstanceOf(BCryptPasswordEncoder.class, context.getBean(PasswordEncoder.class));
    }

    @Test
    @DisplayName("Test: a user saved with hasher should be able to authenticate through the authentication manager")
    void test_AUserSavedWithTheHasherShouldBeAbleToAuthenticateThroughTheAuthenticationManager() {
        String userName = uniqueName("bob");
        saveUser(userName);

        AuthenticatedUser result = authenticator.authenticate(userName, PASSWORD);

        assertEquals(userName, result.userName());
        assertEquals(Set.of("COMMON"), result.roles());
    }

    @Test
    @DisplayName("Test: authentication should fail for a wrong password and for a disabled user")
    void test_AuthenticationShouldFailForAWrongPasswordAndForADisabledUser() {
        String userName = uniqueName("bob");
        User user = saveUser(userName);

        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(userName, "wrong-password"));

        user.setEnabled(false);
        userRepository.save(user);

        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(userName, PASSWORD));
    }

    @Test
    @DisplayName("Test: the seeded admin should be loadable by the authentication manager")
    void test_TheSeededAdminShouldBeLoadableByTheAuthenticationManager() {
        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate("admin", "definitely-wrong"));
    }

    @Test
    @DisplayName("Test: the configured jwt secret should sign tokens that only the right decoder accepts")
    void test_TheConfiguredJwtSecretShouldSignTokensThatOnlyTheRightDecoderAccepts() {
        AuthTokens tokens = tokenService.generateTokens("bob", Set.of("COMMON"));

        assertEquals("bob", jwtDecoder.decode(tokens.accessToken()).getSubject());
        assertThrows(JwtException.class, () -> jwtDecoder.decode(tokens.refreshToken()),
                () -> "the access decoder must reject refresh tokens");
        assertEquals("bob", tokenService.validateRefreshToken(tokens.refreshToken()));
    }
}
