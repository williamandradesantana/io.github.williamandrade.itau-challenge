package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission.PermissionJpaRepository;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission.PermissionMapper;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.permission.PermissionRepositoryImpl;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user.UserJpaRepository;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user.UserMapper;
import io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user.UserRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthPersistenceConfig {

    @Bean
    public UserMapper userMapper(PermissionMapper permissionMapper) {
        return new UserMapper(permissionMapper);
    }

    @Bean
    public PermissionMapper permissionMapper() {
        return new PermissionMapper();
    }

    @Bean
    public PermissionRepository permissionRepository(PermissionJpaRepository jpaRepository, PermissionMapper mapper) {
        return new PermissionRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public UserRepository userRepository(UserJpaRepository jpaRepository, UserMapper mapper) {
        return new UserRepositoryImpl(jpaRepository, mapper);
    }
}
