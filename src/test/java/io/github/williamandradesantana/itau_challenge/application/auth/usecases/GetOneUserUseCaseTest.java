package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.UserResult;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.InMemoryUserRepository;
import io.github.williamandradesantana.itau_challenge.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GetOneUserUseCaseTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private GetOneUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        useCase = new GetOneUserUseCase(userRepository);
    }

    @Test
    @DisplayName("Test: when the user exists, should return its data")
    void test_WhenTheUserExists_ShouldReturnItsData() {
        User bob = UserFixtures.user("bob", permissionRepository.common);
        userRepository.save(bob);

        UserResult result = useCase.execute(bob.getId());

        assertEquals(bob.getId(), result.id());
        assertEquals("bob", result.userName());
        assertEquals("Full bob", result.fullName());
        assertTrue(result.enabled());
        assertEquals(Set.of("COMMON"), result.roles());
    }

    @Test
    @DisplayName("Test: when the user is disabled, should still return it with enabled false")
    void test_WhenTheUserIsDisabled_ShouldStillReturnItWithEnabledFalse() {
        User bob = UserFixtures.user("bob", permissionRepository.common);
        bob.setEnabled(false);
        userRepository.save(bob);

        UserResult result = useCase.execute(bob.getId());

        assertFalse(result.enabled());
    }

    @Test
    @DisplayName("Test: when the user does not exist, should throw exception")
    void test_WhenTheUserDoesNotExist_ShouldThrowException() {
        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));

        assertTrue(exception.getMessage().contains("User not found"));
    }

}