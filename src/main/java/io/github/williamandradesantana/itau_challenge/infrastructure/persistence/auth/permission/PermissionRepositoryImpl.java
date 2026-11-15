package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final PermissionMapper mapper;

    public PermissionRepositoryImpl(PermissionJpaRepository jpaRepository, PermissionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<Permission> listAllPermissions(PageQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());
        Page<PermissionJpaEntity> page = jpaRepository.findAll(pageable);

        List<Permission> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findByPermissionName(String permissionName) {
        return jpaRepository.findByName(permissionName).map(mapper::toDomain);
    }

    @Override
    public void save(Permission permission) {
        jpaRepository.save(mapper.toJpaEntity(permission));
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
