package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission.PermissionJpaEntity;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission.PermissionMapper;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    private final PermissionMapper permissionMapper;

    public UserMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public User toDomain(UserJpaEntity entity) {
        Set<Permission> permissions = entity.getPermissions().stream()
                .map(permissionMapper::toDomain)
                .collect(Collectors.toSet());

        return new User(
            entity.getId(),
            entity.getUserName(),
            entity.getFullName(),
            entity.getPassword(),
            entity.isAccountNonExpired(),
            entity.isAccountNonLocked(),
            entity.isCredentialsNonExpired(),
            entity.isEnabled(),
            permissions
        );
    }

    public UserJpaEntity toJpaEntity(User user) {
        Set<PermissionJpaEntity> permissions = user.getPermissions().stream()
                .map(permissionMapper::toJpaEntity)
                .collect(Collectors.toSet());

        return new UserJpaEntity(
            user.getId(),
            user.getUserName(),
            user.getFullName(),
            user.getPassword(),
            user.isAccountNonExpired(),
            user.isAccountNonLocked(),
            user.isCredentialsNonExpired(),
            user.isEnabled(),
            permissions
        );
    }
}
