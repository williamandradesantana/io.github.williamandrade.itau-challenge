package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission.PermissionJpaEntity;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission.PermissionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private static final String PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ01234";

    private final UserMapper mapper = new UserMapper(new PermissionMapper());

    private final Permission common = Permission.create(new PermissionName("COMMON"));
    private final Permission admin = Permission.create(new PermissionName("ADMIN"));

    private Set<String> namesOf(Set<PermissionJpaEntity> permissions) {
        return permissions.stream().map(PermissionJpaEntity::getName).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Test: toJpaEntity should map every field of the user")
    void test_ToJpaEntityShouldMapEveryFieldOfTheUser() {
        User user = new User(
            UUID.randomUUID(), "bob", "Bob Silva", PASSWORD_HASH,
            true, false, true, false,
            Set.of(common, admin)
        );

        UserJpaEntity entity = mapper.toJpaEntity(user);

        assertEquals(user.getId(), entity.getId());
        assertEquals("bob", entity.getUserName());
        assertEquals("Bob Silva", entity.getFullName());
        assertEquals(PASSWORD_HASH, entity.getPassword());
        assertTrue(entity.isAccountNonExpired());
        assertFalse(entity.isAccountNonLocked());
        assertTrue(entity.isCredentialsNonExpired());
        assertFalse(entity.isEnabled());
        assertEquals(Set.of("COMMON", "ADMIN"), namesOf(entity.getPermissions()));
    }

    @Test
    @DisplayName("Test: toDomain should map every field of the entity")
    void test_ToDomainShouldMapEveryFieldOfTheEntity() {
        UUID id = UUID.randomUUID();
        UserJpaEntity entity = new UserJpaEntity(
            id, "alice", "Alice Souza", PASSWORD_HASH,
            false, true, false, true,
            Set.of(new PermissionJpaEntity(UUID.randomUUID(), "ADMIN"))
        );

        User user = mapper.toDomain(entity);

        assertEquals(id, user.getId());
        assertEquals("alice", user.getUserName());
        assertEquals("Alice Souza", user.getFullName());
        assertEquals(PASSWORD_HASH, user.getPassword());
        assertFalse(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertFalse(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        assertEquals(1, user.getPermissions().size());
        assertEquals("ADMIN", user.getPermissions().iterator().next().permissionName().value());
    }

    @Test
    @DisplayName("Test: converting to entity and back should preserve the user and its permissions")
    void test_ConvertingToEntityAndBackShouldPreserveTheUserAndItsPermissions() {
        User original = User.create("bob", "Bob Silva", PASSWORD_HASH, Set.of(common, admin));
        original.setEnabled(false);

        User roundTrip = mapper.toDomain(mapper.toJpaEntity(original));

        assertEquals(original, roundTrip);
        assertEquals(original.getUserName(), roundTrip.getUserName());
        assertEquals(original.getFullName(), roundTrip.getFullName());
        assertEquals(original.getPassword(), roundTrip.getPassword());
        assertFalse(roundTrip.isEnabled(), () -> "the disabled state must survive the round trip");
        assertEquals(original.getPermissions(), roundTrip.getPermissions());
    }

    @Test
    @DisplayName("Test: a user without permissions should map to an entity without permissions")
    void test_AUserWithoutPermissionsShouldMapToAnEntityWithoutPermissions() {
        User user = User.create("bob", "Bob Silva", PASSWORD_HASH, Set.of());

        UserJpaEntity entity = mapper.toJpaEntity(user);

        assertTrue(entity.getPermissions().isEmpty());
        assertTrue(mapper.toDomain(entity).getPermissions().isEmpty());
    }

    @Test
    @DisplayName("Test: the password hash must be copied untouched")
    void test_ThePasswordHashMustBeCopiedUntouched() {
        User user = User.create("bob", "Bob Silva", PASSWORD_HASH, Set.of(common));

        assertSame(PASSWORD_HASH, mapper.toJpaEntity(user).getPassword());
    }
}
