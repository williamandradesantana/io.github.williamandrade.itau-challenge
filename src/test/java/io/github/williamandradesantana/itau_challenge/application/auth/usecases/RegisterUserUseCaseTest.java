package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.RegisterUserCommand;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidFullNameException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidPasswordException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidUserNameException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.UserNameAlreadyExistsException;
import io.github.williamandradesantana.itau_challenge.support.FakePasswordHasher;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.InMemoryUserRepository;
import io.github.williamandradesantana.itau_challenge.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUserUseCaseTest {

    private static final String PASSWORD = "Senha@123";

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private FakePasswordHasher passwordHasher;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        passwordHasher = new FakePasswordHasher();
        useCase = new RegisterUserUseCase(userRepository, permissionRepository, passwordHasher);
    }

    @Test
    @DisplayName("Test: when the command is valid, should save the user with the common permission")
    void test_WhenTheCommandIsValid_ShouldSaveTheUserWithTheCommonPermission() {
        useCase.execute(new RegisterUserCommand("bob", "Bob Silva", PASSWORD));

        User saved = userRepository.findByUserName("bob").orElseThrow();
        assertEquals(1, userRepository.count());
        assertEquals("Bob Silva", saved.getFullName());
        assertEquals(1, saved.getPermissions().size(), () -> "the user must have exactly one permission");
        assertTrue(saved.getPermissions().contains(permissionRepository.common), () -> "the permission must be COMMON");
        assertTrue(saved.isEnabled());
    }

    @Test
    @DisplayName("Test: should store the password hash and never the raw password")
    void test_ShouldStoreThePasswordHashAndNeverTheRawPassword() {
        useCase.execute(new RegisterUserCommand("bob", "Bob Silva", PASSWORD));

        User saved = userRepository.findByUserName("bob").orElseThrow();
        assertNotEquals(PASSWORD, saved.getPassword());
        assertEquals(passwordHasher.hash(PASSWORD), saved.getPassword());
    }

    @Test
    @DisplayName("Test: should trim user name and full name before saving")
    void test_ShouldTrimUserNameAndFullNameBeforeSaving() {
        useCase.execute(new RegisterUserCommand("  bob  ", "  Bob Silva  ", PASSWORD));

        User saved = userRepository.findByUserName("bob").orElseThrow();
        assertEquals("bob", saved.getUserName());
        assertEquals("Bob Silva", saved.getFullName());
    }

    @Test
    @DisplayName("Test: when the command is null, should throw exception")
    void test_WhenTheCommandIsNull_ShouldThrowException() {
        assertThrows(RequiredObjectIsNullException.class, () -> useCase.execute(null));
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("Test: when the password is invalid, should throw exception and save nothing")
    void test_WhenThePasswordIsInvalid_ShouldThrowExceptionAndSaveNothing() {
        String[] invalidPasswords = {null, "", "   ", "1", "1234567", "a".repeat(73)};

        for (String invalid : invalidPasswords) {
            assertThrows(InvalidPasswordException.class,
                    () -> useCase.execute(new RegisterUserCommand("bob", "Bob Silva", invalid)),
                    () -> "password should have been rejected: " + invalid);
        }
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("Test: when the user name is invalid, should throw exception and save nothing")
    void test_WhenTheUserNameIsInvalid_ShouldThrowExceptionAndSaveNothing() {
        String[] invalidUserNames = {null, "", "   ", "ab", "a".repeat(31)};

        for (String invalid : invalidUserNames) {
            assertThrows(InvalidUserNameException.class,
                    () -> useCase.execute(new RegisterUserCommand(invalid, "Bob Silva", PASSWORD)),
                    () -> "user name should have been rejected: " + invalid);
        }
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("Test: when the full name is invalid, should throw exception and save nothing")
    void test_WhenTheFullNameIsInvalid_ShouldThrowExceptionAndSaveNothing() {
        assertThrows(InvalidFullNameException.class, () -> useCase.execute(new RegisterUserCommand("bob", null, PASSWORD)));
        assertThrows(InvalidFullNameException.class, () -> useCase.execute(new RegisterUserCommand("bob", "  ", PASSWORD)));
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("Test: when the user name already exists, should throw exception")
    void test_WhenTheUserNameAlreadyExists_ShouldThrowException() {
        useCase.execute(new RegisterUserCommand("bob", "Bob Silva", PASSWORD));

        UserNameAlreadyExistsException exception = assertThrows(UserNameAlreadyExistsException.class,
                () -> useCase.execute(new RegisterUserCommand("bob", "Another Bob", PASSWORD)));

        assertEquals("UserName already exists: bob", exception.getMessage());
        assertEquals(1, userRepository.count());
    }

    @Test
    @DisplayName("Test: when the user name differs only by surrounding spaces, should be treated as duplicate")
    void test_WhenTheUserNameDiffersOnlyBySurroundingSpaces_ShouldBeTreatedAsDuplicate() {
        useCase.execute(new RegisterUserCommand("bob", "Bob Silva", PASSWORD));

        assertThrows(UserNameAlreadyExistsException.class,
                () -> useCase.execute(new RegisterUserCommand("  bob  ", "Bob Silva", PASSWORD)));
        assertEquals(1, userRepository.count());
    }

    @Test
    @DisplayName("Test: when two registrations race and the database rejects the duplicate, should propagate the exception")
    void test_WhenTwoRegistrationsRaceAndTheDatabaseRejectsTheDuplicate_ShouldPropagateTheException() {
        InMemoryUserRepository racingRepository = new InMemoryUserRepository() {
            @Override
            public Optional<User> findByUserName(String userName) {
                return Optional.empty();
            }
        }.enforceUniqueUserName();
        racingRepository.save(UserFixtures.user("bob", permissionRepository.common));
        RegisterUserUseCase racingUseCase = new RegisterUserUseCase(racingRepository, permissionRepository, passwordHasher);

        assertThrows(UserNameAlreadyExistsException.class,
                () -> racingUseCase.execute(new RegisterUserCommand("bob", "Bob Silva", PASSWORD)));
        assertEquals(1, racingRepository.count());
    }

    @Test
    @DisplayName("Test: when the default permission was not seeded, should throw exception")
    void test_WhenTheDefaultPermissionWasNotSeeded_ShouldThrowException() {
        permissionRepository.clear();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> useCase.execute(new RegisterUserCommand("bob", "Bob Silva", PASSWORD)));

        assertEquals("Default permission not seeded: COMMON", exception.getMessage());
        assertEquals(0, userRepository.count());
    }
}