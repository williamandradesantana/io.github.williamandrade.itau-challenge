package io.github.williamandradesantana.itau_challenge.infrastructure.security;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.InMemoryUserRepository;
import io.github.williamandradesantana.itau_challenge.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DomainUserDetailsServiceTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private DomainUserDetailsService service;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        service = new DomainUserDetailsService(userRepository);
    }

    private User saveUser(String userName) {
        User user = UserFixtures.user(userName, permissionRepository.common);
        userRepository.save(user);
        return user;
    }

    private Set<String> authoritiesOf(UserDetails details) {
        return details.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Test: when the user exists, should return its username and stored password hash")
    void test_WhenTheUserExists_ShouldReturnItsUsernameAndStoredPasswordHash() {
        User bob = saveUser("bob");

        UserDetails details = service.loadUserByUsername("bob");

        assertEquals("bob", details.getUsername());
        assertEquals(bob.getPassword(), details.getPassword(), () -> "the stored hash must be passed untouched");
    }

    @Test
    @DisplayName("Test: the authorities should be the plain permission names, without the ROLE_ prefix")
    void test_TheAuthoritiesShouldBeThePlainPermissionNames() {
        userRepository.save(UserFixtures.user("alice", permissionRepository.admin, permissionRepository.common));

        UserDetails details = service.loadUserByUsername("alice");

        assertEquals(Set.of("ADMIN", "COMMON"), authoritiesOf(details));
    }

    @Test
    @DisplayName("Test: an active user should have every account flag enabled")
    void test_AnActiveUserShouldHaveEveryAccountFlagEnabled() {
        saveUser("bob");

        UserDetails details = service.loadUserByUsername("bob");

        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Test: a disabled user should be reported as disabled")
    void test_ADisabledUserShouldBeReportedAsDisabled() {
        saveUser("bob").setEnabled(false);

        assertFalse(service.loadUserByUsername("bob").isEnabled());
    }

    @Test
    @DisplayName("Test: a locked user should be reported as locked")
    void test_ALockedUserShouldBeReportedAsLocked() {
        saveUser("bob").setAccountNonLocked(false);

        assertFalse(service.loadUserByUsername("bob").isAccountNonLocked());
    }

    @Test
    @DisplayName("Test: an expired account should be reported as expired")
    void test_AnExpiredAccountShouldBeReportedAsExpired() {
        saveUser("bob").setAccountNonExpired(false);

        assertFalse(service.loadUserByUsername("bob").isAccountNonExpired());
    }

    @Test
    @DisplayName("Test: expired credentials should be reported as expired")
    void test_ExpiredCredentialsShouldBeReportedAsExpired() {
        saveUser("bob").setCredentialsNonExpired(false);

        assertFalse(service.loadUserByUsername("bob").isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Test: surrounding spaces in the username should be ignored")
    void test_SurroundingSpacesInTheUsernameShouldBeIgnored() {
        saveUser("bob");

        assertEquals("bob", service.loadUserByUsername("  bob  ").getUsername());
    }

    @Test
    @DisplayName("Test: when the user does not exist, should throw exception")
    void test_WhenTheUserDoesNotExist_ShouldThrowException() {
        saveUser("bob");

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
    }
}
