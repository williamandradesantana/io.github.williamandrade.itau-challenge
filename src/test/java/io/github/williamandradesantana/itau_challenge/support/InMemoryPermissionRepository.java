package io.github.williamandradesantana.itau_challenge.support;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;

import java.util.*;

public class InMemoryPermissionRepository implements PermissionRepository {

    public final Permission common = Permission.create(new PermissionName("COMMON"));
    public final Permission admin = Permission.create(new PermissionName("ADMIN"));

    private final Map<UUID, Permission> permissions = new LinkedHashMap<>();

    public InMemoryPermissionRepository() {
        save(common);
        save(admin);
    }

    public void clear() {
        permissions.clear();
    }

    @Override
    public PageResult<Permission> listAllPermissions(PageQuery query) {
        List<Permission> list = permissions.values().stream()
                .sorted(Comparator.comparing(permission -> permission.permissionName().value()))
                .toList();

        int from = Math.min(query.page() * query.size(), list.size());
        int to = Math.min(from + query.size(), list.size());
        return new PageResult<>(list.subList(from, to), query.page(), query.size(), list.size());
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return Optional.ofNullable(permissions.get(id));
    }

    @Override
    public Optional<Permission> findByPermissionName(String permissionName) {
        return permissions.values().stream()
                .filter(permission -> permission.permissionName().value().equals(permissionName))
                .findFirst();
    }

    @Override
    public void save(Permission permission) {
        permissions.put(permission.id(), permission);
    }

    @Override
    public void delete(UUID id) {
        permissions.remove(id);
    }
}
