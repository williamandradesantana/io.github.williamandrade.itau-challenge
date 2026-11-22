package io.github.williamandradesantana.itau_challenge.infrastructure.security;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordHasherTest {

    private static final String PASSWORD = "Senha@123";

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(new BCryptPasswordEncoder(4));

    @Test
    @DisplayName("Test: hash should not return the raw password")
    void test_HashShouldNotReturnTheRawPassword() {
        assertNotEquals(PASSWORD, hasher.hash(PASSWORD));
    }

    @Test
    @DisplayName("Test: hash should be a bcrypt hash with sixty characters")
    void test_HashShouldBeABcryptHashWithSixtyCharacters() {
        String hash = hasher.hash(PASSWORD);

        assertTrue(hash.startsWith("$2"), () -> "expected a bcrypt hash but got: " + hash);
        assertEquals(60, hash.length());
    }

    @Test
    @DisplayName("Test: the hash should be accepted as the password of a domain user")
    void test_TheHashShouldBeAcceptedAsThePasswordOfADomainUser() {
        String hash = hasher.hash(PASSWORD);

        assertDoesNotThrow(() -> User.create("bob", "Bob Silva", hash, Set.of()));
    }

    @Test
    @DisplayName("Test: hashing the same password twice should give different hashes")
    void test_HashingTheSamePasswordTwiceShouldGiveDifferentHashes() {
        assertNotEquals(hasher.hash(PASSWORD), hasher.hash(PASSWORD), () -> "each hash must use its own salt");
    }

    @Test
    @DisplayName("Test: matches should be true for the original password")
    void test_MatchesShouldBeTrueForTheOriginalPassword() {
        assertTrue(hasher.matches(PASSWORD, hasher.hash(PASSWORD)));
    }

    @Test
    @DisplayName("Test: matches should be false for a different password")
    void test_MatchesShouldBeFalseForADifferentPassword() {
        String hash = hasher.hash(PASSWORD);

        assertFalse(hasher.matches("outra-senha", hash));
        assertFalse(hasher.matches(PASSWORD.toLowerCase(), hash));
        assertFalse(hasher.matches("", hash));
    }

    @Test
    @DisplayName("Test: matches should be false when the stored value is not a bcrypt hash")
    void test_MatchesShouldBeFalseWhenTheStoredValueIsNotABcryptHash() {
        assertFalse(hasher.matches(PASSWORD, PASSWORD));
        assertFalse(hasher.matches(PASSWORD, "not-a-bcrypt-hash"));
    }

    @Test
    @DisplayName("Test: matches should verify hashes created with a different cost")
    void test_MatchesShouldVerifyHashesCreatedWithADifferentCost() {
        String hashWithDefaultCost = new BCryptPasswordEncoder().encode(PASSWORD);

        assertTrue(hasher.matches(PASSWORD, hashWithDefaultCost));
    }
}
