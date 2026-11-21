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

import static org.junit.jupiter.api.Assertions.*;

class GetAUserByNameUseCaseTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private GetAUserByNameUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        useCase = new GetAUserByNameUseCase(userRepository);
    }

    @Test
    @DisplayName("Test: when the user exists, should return its data")
    void test_WhenTheUserExists_ShouldReturnItsData() {
        User alice = UserFixtures.user("alice", permissionRepository.admin);
        userRepository.save(alice);
        userRepository.save(UserFixtures.user("bob", permissionRepository.common));

        UserResult result = useCase.execute("alice");

        assertEquals(alice.getId(), result.id());
        assertEquals("alice", result.userName());
        assertEquals(Set.of("ADMIN"), result.roles());
    }

    @Test
    @DisplayName("Test: when the user does not exist, should throw exception")
    void test_WhenTheUserDoesNotExist_ShouldThrowException() {
        userRepository.save(UserFixtures.user("bob", permissionRepository.common));

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> useCase.execute("ghost"));

        assertTrue(exception.getMessage().contains("User not found"));
    }

}