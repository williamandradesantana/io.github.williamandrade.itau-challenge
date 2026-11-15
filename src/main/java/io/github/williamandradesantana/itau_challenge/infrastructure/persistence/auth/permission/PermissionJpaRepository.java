package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, UUID> {
    Optional<PermissionJpaEntity> findByName(String name);
}
