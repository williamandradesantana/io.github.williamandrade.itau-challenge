package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;

public final class PermissionMapper {

    public Permission toDomain(PermissionJpaEntity entity) {
        return new Permission(entity.getId(), new PermissionName(entity.getName()));
    }

    public PermissionJpaEntity toJpaEntity(Permission permission) {
        return new PermissionJpaEntity(permission.id(), permission.permissionName().value());
    }
}
