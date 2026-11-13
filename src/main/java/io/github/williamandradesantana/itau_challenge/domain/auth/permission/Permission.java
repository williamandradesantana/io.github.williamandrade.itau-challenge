package io.github.williamandradesantana.itau_challenge.domain.auth.permission;

import java.util.Objects;
import java.util.UUID;

public record Permission(UUID id, PermissionName permissionName) {

    public Permission {
        Objects.requireNonNull(id);
        Objects.requireNonNull(permissionName);
    }

    public static Permission create(PermissionName permissionName) {
        return new Permission(UUID.randomUUID(), permissionName);
    }

    public Permission update(PermissionName permissionName) {
        return new Permission(this.id, permissionName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
