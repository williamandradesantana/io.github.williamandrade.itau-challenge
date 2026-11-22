package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionMapperTest {

    private final PermissionMapper mapper = new PermissionMapper();

    @Test
    @DisplayName("Test: toJpaEntity should map the id and the name")
    void test_ToJpaEntityShouldMapTheIdAndTheName() {
        Permission permission = Permission.create(new PermissionName("ADMIN"));

        PermissionJpaEntity entity = mapper.toJpaEntity(permission);

        assertEquals(permission.id(), entity.getId());
        assertEquals("ADMIN", entity.getName());
    }

    @Test
    @DisplayName("Test: toDomain should map the id and the name")
    void test_ToDomainShouldMapTheIdAndTheName() {
        UUID id = UUID.randomUUID();

        Permission permission = mapper.toDomain(new PermissionJpaEntity(id, "COMMON"));

        assertEquals(id, permission.id());
        assertEquals("COMMON", permission.permissionName().value());
    }

    @Test
    @DisplayName("Test: converting to entity and back should give an equal permission")
    void test_ConvertingToEntityAndBackShouldGiveAnEqualPermission() {
        Permission original = Permission.create(new PermissionName("COMMON"));

        Permission roundTrip = mapper.toDomain(mapper.toJpaEntity(original));

        assertEquals(original, roundTrip);
        assertEquals(original.permissionName(), roundTrip.permissionName());
    }
}
