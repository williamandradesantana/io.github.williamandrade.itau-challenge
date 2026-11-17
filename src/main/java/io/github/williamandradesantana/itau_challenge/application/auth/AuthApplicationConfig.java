package io.github.williamandradesantana.itau_challenge.application.auth;

import io.github.williamandradesantana.itau_challenge.application.auth.usecases.*;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthApplicationConfig {

    @Bean
    public LoginUseCase loginUseCase(
        IAuthenticator authenticator,
        ITokenService tokenService
    ) {
        return new LoginUseCase(authenticator, tokenService);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
        UserRepository userRepository,
        ITokenService tokenService
    ) {
        return new RefreshTokenUseCase(userRepository, tokenService);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
        UserRepository userRepository,
        PermissionRepository permissionRepository,
        IPasswordHasher passwordHasher
    ) {
        return new RegisterUserUseCase(userRepository, permissionRepository, passwordHasher);
    }

    @Bean
    public ListAllUsersUseCase listAllUsersUseCase(UserRepository userRepository) {
        return new ListAllUsersUseCase(userRepository);
    }

    @Bean
    public GetOneUserUseCase getOneUserUseCase(UserRepository userRepository) {
        return new GetOneUserUseCase(userRepository);
    }

    @Bean
    public GetAUserByNameUseCase getAUserByName(UserRepository userRepository) {
        return new GetAUserByNameUseCase(userRepository);
    }

    @Bean
    public DisableUserUseCase disableUserUseCase(UserRepository userRepository) {
        return new DisableUserUseCase(userRepository);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUserUseCase(userRepository);
    }
}
