package io.github.williamandradesantana.itau_challenge.domain.auth.permission;

import io.github.williamandradesantana.itau_challenge.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.shared.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    PageResult<Permission> listAllPermissions(PageQuery query);
    Optional<Permission> findById(UUID id);
    void save(Permission permission);
    void delete(UUID id);
}
