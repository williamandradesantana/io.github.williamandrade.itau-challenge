package io.github.williamandradesantana.itau_challenge.domain.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.exceptions.InvalidPermissionNameException;

public record PermissionName(String value) {

    public PermissionName {
        if (value == null || value.isBlank())
            throw new InvalidPermissionNameException("The permission name cannot be null or blank");

        value = value.trim();

        if (value.length() > 20)
            throw new InvalidPermissionNameException("The permission name cannot exceed 20 characters");
    }
}