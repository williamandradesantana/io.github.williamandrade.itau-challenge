package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByUserName(String userName);

    @Modifying(clearAutomatically = true)
    @Query("update UserJpaEntity u set u.enabled = false where u.id = :id")
    void disableUser(@Param("id") UUID id);
}
