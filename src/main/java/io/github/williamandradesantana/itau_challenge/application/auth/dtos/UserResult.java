package io.github.williamandradesantana.itau_challenge.application.auth.dtos;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResult(UUID id, String userName, String fullName, boolean enabled, Set<String> roles) {
    public static UserResult from(User user) {
        Set<String> roles = user.getPermissions().stream()
                .map(Permission::permissionName)
                .map(PermissionName::value)
                .collect(Collectors.toSet());

        return new UserResult(user.getId(), user.getUserName(), user.getFullName(), user.isEnabled(), roles);
    }
}
