package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.ITokenService;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.LoginCommand;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.interfaces.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.interfaces.shared.exceptions.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Set;
import java.util.stream.Collectors;

public class LoginUseCase {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final ITokenService tokenService;

    public LoginUseCase(UserRepository userRepository, AuthenticationManager authenticationManager, ITokenService tokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public String execute(LoginCommand command) {
        if (command == null) throw new RequiredObjectIsNullException();

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(command.userName(), command.password()));

        User user = userRepository.findByUserName(command.userName()).orElseThrow(
                () -> new ResourceNotFoundException("User with username: " + command.userName() + " not found!")
        );

        Set<String> roles = user.getPermissions().stream()
                .map(Permission::permissionName)
                .map(PermissionName::value)
                .collect(Collectors.toSet());

        return tokenService.generateToken(command.userName(), roles);
    }
}
