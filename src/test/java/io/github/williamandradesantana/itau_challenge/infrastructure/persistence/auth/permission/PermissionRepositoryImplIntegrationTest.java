package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.PostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PermissionRepositoryImplIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    @DisplayName("Test: the seeded permissions should be found by name")
    void test_TheSeededPermissionsShouldBeFoundByName() {
        Permission common = permissionRepository.findByPermissionName("COMMON").orElseThrow();
        Permission admin = permissionRepository.findByPermissionName("ADMIN").orElseThrow();

        assertEquals("COMMON", common.permissionName().value());
        assertEquals("ADMIN", admin.permissionName().value());
        assertNotEquals(common.id(), admin.id());
    }

    @Test
    @DisplayName("Test: when the permission name does not exist, should return empty")
    void test_WhenThePermissionNameDoesNotExist_ShouldReturnEmpty() {
        assertTrue(permissionRepository.findByPermissionName("NOPE").isEmpty());
        assertTrue(permissionRepository.findByPermissionName("common").isEmpty(), () -> "the search is case sensitive");
    }

    @Test
    @DisplayName("Test: a permission should be found by id")
    void test_APermissionShouldBeFoundById() {
        Permission common = permissionRepository.findByPermissionName("COMMON").orElseThrow();

        assertEquals(common, permissionRepository.findById(common.id()).orElseThrow());
        assertTrue(permissionRepository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Test: listAllPermissions should return both permissions")
    void test_ListAllPermissionsShouldReturnBothPermissions() {
        PageResult<Permission> page = permissionRepository.listAllPermissions(new PageQuery(0, 10));

        Set<String> names = page.content().stream().map(
            permission -> permission.permissionName().value()).collect(Collectors.toSet()
        );
        assertEquals(Set.of("COMMON", "ADMIN"), names);
        assertEquals(2L, page.totalElements());
    }

    @Test
    @DisplayName("Test: listAllPermissions should paginate")
    void test_ListAllPermissionsShouldPaginate() {
        PageResult<Permission> first = permissionRepository.listAllPermissions(new PageQuery(0, 1));
        PageResult<Permission> second = permissionRepository.listAllPermissions(new PageQuery(1, 1));

        assertEquals(1, first.content().size());
        assertTrue(first.hasNext());
        assertEquals(1, second.content().size());
        assertFalse(second.hasNext());
        assertNotEquals(first.content().get(0), second.content().get(0));
    }

    @Test
    @DisplayName("Test: saving an existing permission should not create a duplicate")
    void test_SavingAnExistingPermissionShouldNotCreateADuplicate() {
        Permission common = permissionRepository.findByPermissionName("COMMON").orElseThrow();

        permissionRepository.save(common);

        assertEquals(2L, permissionRepository.listAllPermissions(new PageQuery(0, 10)).totalElements());
    }
}
