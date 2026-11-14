package io.github.williamandradesantana.itau_challenge.domain.auth.permission;

import io.github.williamandradesantana.itau_challenge.interfaces.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.interfaces.shared.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    PageResult<Permission> listAllPermissions(PageQuery query);
    Optional<Permission> findById(UUID id);
    Optional<Permission> findByPermissionName(String permissionName);
    void save(Permission permission);
    void delete(UUID id);
}
