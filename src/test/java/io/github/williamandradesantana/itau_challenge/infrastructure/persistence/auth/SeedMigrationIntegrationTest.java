package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.PostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeedMigrationIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Set<String> permissionsOf(User user) {
        return user.getPermissions().stream().map(permission -> permission.permissionName().value()).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Test: every migration should have been applied successfully")
    void test_EveryMigrationShouldHaveBeenAppliedSuccessfully() {
        Integer total = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history", Integer.class);
        Integer failed = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history where success = false", Integer.class);

        assertTrue(total >= 5, () -> "expected at least the 5 migrations, found " + total);
        assertEquals(0, failed);
    }

    @Test
    @DisplayName("Test: each seeded permission should exist exactly once")
    void test_EachSeededPermissionShouldExistExactlyOnce() {
        for (String name : new String[]{"COMMON", "ADMIN"}) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from tb_permissions where name = ?", Integer.class, name);
            assertEquals(1, count, () -> "permission " + name + " must exist exactly once");
        }
    }

    @Test
    @DisplayName("Test: the seeded admin should be active and have only the admin permission")
    void test_TheSeededAdminShouldBeActiveAndHaveOnlyTheAdminPermission() {
        User admin = userRepository.findByUserName("admin").orElseThrow();

        assertTrue(admin.isEnabled());
        assertTrue(admin.isAccountNonLocked());
        assertTrue(admin.isAccountNonExpired());
        assertTrue(admin.isCredentialsNonExpired());
        assertEquals(Set.of("ADMIN"), permissionsOf(admin));
    }

    @Test
    @DisplayName("Test: the seeded common user should be active and have only the common permission")
    void test_TheSeededCommonUserShouldBeActiveAndHaveOnlyTheCommonPermission() {
        User user = userRepository.findByUserName("user").orElseThrow();

        assertTrue(user.isEnabled());
        assertEquals(Set.of("COMMON"), permissionsOf(user));
    }

    @Test
    @DisplayName("Test: the seeded passwords should be stored as bcrypt hashes, never in plain text")
    void test_TheSeededPasswordsShouldBeStoredAsBcryptHashes() {
        for (String userName : new String[]{"admin", "user"}) {
            String password = userRepository.findByUserName(userName).orElseThrow().getPassword();

            assertTrue(password.startsWith("$2"), () -> userName + " password is not a bcrypt hash");
            assertEquals(60, password.length());
        }
    }

    @Test
    @DisplayName("Test: the database should reject a permission name outside COMMON and ADMIN")
    void test_TheDatabaseShouldRejectAPermissionNameOutsideCommonAndAdmin() {
        assertThrows(DataIntegrityViolationException.class,
            () -> jdbcTemplate.update("insert into tb_permissions (id, name) values (gen_random_uuid(), 'HACKER')"));
    }

    @Test
    @DisplayName("Test: the database should reject a duplicated user name")
    void test_TheDatabaseShouldRejectADuplicatedUserName() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
            "insert into tb_users (id, user_name, full_name, password) values (gen_random_uuid(), 'admin', 'Another', 'x')"));
    }
}
