package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.UserNameAlreadyExistsException;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryImplIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private Permission permission(String name) {
        return permissionRepository.findByPermissionName(name).orElseThrow();
    }

    private String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private User newUser(String userName, Permission... permissions) {
        return User.create(userName, "Full " + userName, "stored-password-hash", Set.of(permissions));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Test: a saved user should be found by name with all its fields and permissions")
    void test_ASavedUserShouldBeFoundByNameWithAllItsFieldsAndPermissions() {
        String userName = uniqueName("bob");
        User user = newUser(userName, permission("COMMON"), permission("ADMIN"));
        user.setAccountNonLocked(false);
        user.setCredentialsNonExpired(false);
        userRepository.save(user);
        flushAndClear();

        User found = userRepository.findByUserName(userName).orElseThrow();

        assertEquals(user.getId(), found.getId());
        assertEquals("Full " + userName, found.getFullName());
        assertEquals("stored-password-hash", found.getPassword());
        assertTrue(found.isEnabled());
        assertTrue(found.isAccountNonExpired());
        assertFalse(found.isAccountNonLocked(), () -> "the locked flag must be persisted");
        assertFalse(found.isCredentialsNonExpired(), () -> "the credentials flag must be persisted");
        assertEquals(Set.of("COMMON", "ADMIN"), namesOf(found));
    }

    @Test
    @DisplayName("Test: a saved user should be found by id")
    void test_ASavedUserShouldBeFoundById() {
        User user = newUser(uniqueName("bob"), permission("COMMON"));
        userRepository.save(user);
        flushAndClear();

        assertEquals(user.getUserName(), userRepository.findById(user.getId()).orElseThrow().getUserName());
    }

    @Test
    @DisplayName("Test: when the user does not exist, find should return empty")
    void test_WhenTheUserDoesNotExist_FindShouldReturnEmpty() {
        assertTrue(userRepository.findById(UUID.randomUUID()).isEmpty());
        assertTrue(userRepository.findByUserName(uniqueName("ghost")).isEmpty());
    }

    @Test
    @DisplayName("Test: a user without permissions can be saved")
    void test_AUserWithoutPermissionsCanBeSaved() {
        User user = newUser(uniqueName("bob"));
        userRepository.save(user);
        flushAndClear();

        assertTrue(userRepository.findById(user.getId()).orElseThrow().getPermissions().isEmpty());
    }

    @Test
    @DisplayName("Test: saving a user with a name already in use should throw the domain exception")
    void test_SavingAUserWithANameAlreadyInUse_ShouldThrowTheDomainException() {
        String userName = uniqueName("bob");
        userRepository.save(newUser(userName, permission("COMMON")));
        entityManager.flush();

        UserNameAlreadyExistsException exception = assertThrows(UserNameAlreadyExistsException.class,
            () -> userRepository.save(newUser(userName, permission("COMMON"))));

        assertEquals("UserName already exists: " + userName, exception.getMessage());
    }

    @Test
    @DisplayName("Test: saving an existing user should update it instead of creating another one")
    void test_SavingAnExistingUserShouldUpdateItInsteadOfCreatingAnotherOne() {
        User user = newUser(uniqueName("bob"), permission("COMMON"));
        userRepository.save(user);
        flushAndClear();
        long before = userRepository.listAllUsers(new PageQuery(0, 1)).totalElements();

        user.setFullName("New Full Name");
        user.setEnabled(false);
        user.grantPermission(permission("ADMIN"));
        userRepository.save(user);
        flushAndClear();

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("New Full Name", updated.getFullName());
        assertFalse(updated.isEnabled());
        assertEquals(Set.of("COMMON", "ADMIN"), namesOf(updated));
        assertEquals(before, userRepository.listAllUsers(new PageQuery(0, 1)).totalElements(), () -> "no new row must be created");
    }

    @Test
    @DisplayName("Test: disable should disable only the given user")
    void test_DisableShouldDisableOnlyTheGivenUser() {
        User target = newUser(uniqueName("target"), permission("COMMON"));
        User other = newUser(uniqueName("other"), permission("COMMON"));
        userRepository.save(target);
        userRepository.save(other);
        flushAndClear();

        userRepository.disable(target.getId());

        assertFalse(userRepository.findById(target.getId()).orElseThrow().isEnabled());
        assertTrue(userRepository.findById(other.getId()).orElseThrow().isEnabled(), () -> "other users must not be affected");
    }

    @Test
    @DisplayName("Test: delete should remove the user and its permission links")
    void test_DeleteShouldRemoveTheUserAndItsPermissionLinks() {
        User user = newUser(uniqueName("bob"), permission("COMMON"), permission("ADMIN"));
        userRepository.save(user);
        flushAndClear();
        assertEquals(2, countPermissionLinks(user.getId()), () -> "precondition: the links must exist");

        userRepository.delete(user.getId());
        flushAndClear();

        assertTrue(userRepository.findById(user.getId()).isEmpty());
        assertEquals(0, countPermissionLinks(user.getId()));
        assertEquals(2, permissionRepository.listAllPermissions(new PageQuery(0, 10)).totalElements(),
            () -> "the permissions themselves must not be deleted");
    }

    @Test
    @DisplayName("Test: listAllUsers should return users ordered by user name")
    void test_ListAllUsersShouldReturnUsersOrderedByUserName() {
        userRepository.save(newUser(uniqueName("ccc"), permission("COMMON")));
        userRepository.save(newUser(uniqueName("aaa"), permission("COMMON")));
        userRepository.save(newUser(uniqueName("bbb"), permission("COMMON")));
        flushAndClear();

        PageResult<User> page = userRepository.listAllUsers(new PageQuery(0, 100));

        List<String> names = page.content().stream().map(User::getUserName).toList();
        assertEquals(names.stream().sorted().toList(), names, () -> "users must come ordered by user name");
        assertEquals(page.totalElements(), names.size());
    }

    @Test
    @DisplayName("Test: listAllUsers should paginate")
    void test_ListAllUsersShouldPaginate() {
        for (int i = 0; i < 3; i++) userRepository.save(newUser(uniqueName("user" + i), permission("COMMON")));
        flushAndClear();

        PageResult<User> firstPage = userRepository.listAllUsers(new PageQuery(0, 2));
        PageResult<User> secondPage = userRepository.listAllUsers(new PageQuery(1, 2));

        assertEquals(2, firstPage.content().size());
        assertTrue(firstPage.totalElements() >= 5, () -> "the 2 seeded users plus the 3 created ones");
        assertTrue(firstPage.hasNext());
        assertNotEquals(firstPage.content().get(0).getId(), secondPage.content().get(0).getId());
    }

    @Test
    @DisplayName("Test: a page beyond the last one should be empty")
    void test_APageBeyondTheLastOneShouldBeEmpty() {
        PageResult<User> page = userRepository.listAllUsers(new PageQuery(500, 10));

        assertTrue(page.content().isEmpty());
        assertTrue(page.totalElements() >= 2);
    }

    @Test
    @DisplayName("Test: disabled users should also be listed")
    void test_DisabledUsersShouldAlsoBeListed() {
        User disabled = newUser(uniqueName("off"), permission("COMMON"));
        disabled.setEnabled(false);
        userRepository.save(disabled);
        flushAndClear();

        PageResult<User> page = userRepository.listAllUsers(new PageQuery(0, 100));

        assertTrue(page.content().stream().anyMatch(user -> user.getId().equals(disabled.getId())));
    }

    private Set<String> namesOf(User user) {
        return user.getPermissions().stream().map(permission -> permission.permissionName().value()).collect(Collectors.toSet());
    }

    private Integer countPermissionLinks(UUID userId) {
        return jdbcTemplate.queryForObject("select count(*) from tb_user_permissions where user_id = ?", Integer.class, userId);
    }
}
