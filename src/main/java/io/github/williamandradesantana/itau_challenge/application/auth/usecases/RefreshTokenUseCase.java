package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.ITokenService;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthTokens;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.RefreshTokenCommand;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidRefreshTokenException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final UserRepository userRepository;
    private final ITokenService tokenService;

    public RefreshTokenUseCase(UserRepository userRepository, ITokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public AuthTokens execute(RefreshTokenCommand command) {
        if (command == null) throw new RequiredObjectIsNullException();
        if (command.refreshToken() == null || command.refreshToken().isBlank())
            throw new InvalidRefreshTokenException();

        String userName = tokenService.validateRefreshToken(command.refreshToken());

        User user = userRepository.findByUserName(userName).orElseThrow(InvalidRefreshTokenException::new);

        boolean accountUsable = user.isEnabled()
                && user.isAccountNonLocked()
                && user.isAccountNonExpired()
                && user.isCredentialsNonExpired();

        if (!accountUsable) throw new InvalidRefreshTokenException();

        Set<String> roles = user.getPermissions().stream()
                .map(Permission::permissionName)
                .map(PermissionName::value)
                .collect(Collectors.toSet());

        log.info("Refreshing tokens for user: {}", userName);
        return tokenService.generateTokens(user.getUserName(), roles);
    }
}
