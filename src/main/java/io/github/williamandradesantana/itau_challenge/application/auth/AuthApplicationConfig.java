package io.github.williamandradesantana.itau_challenge.application.auth;

import io.github.williamandradesantana.itau_challenge.application.auth.usecases.RegisterUserUseCase;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthApplicationConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
        UserRepository userRepository,
        PermissionRepository permissionRepository,
        IPasswordHasher passwordHasher
    ) {
        return new RegisterUserUseCase(userRepository, permissionRepository, passwordHasher);
    }
}
