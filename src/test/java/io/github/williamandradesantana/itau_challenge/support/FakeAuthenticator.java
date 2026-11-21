package io.github.williamandradesantana.itau_challenge.support;

import io.github.williamandradesantana.itau_challenge.application.auth.IAuthenticator;
import io.github.williamandradesantana.itau_challenge.application.auth.IPasswordHasher;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthenticatedUser;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidCredentialsException;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;

import java.util.Set;
import java.util.stream.Collectors;

public class FakeAuthenticator implements IAuthenticator {

    private final UserRepository userRepository;
    private final IPasswordHasher passwordHasher;

    public FakeAuthenticator(UserRepository userRepository, IPasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public AuthenticatedUser authenticate(String userName, String password) {
        if (userName == null || userName.isBlank() || password == null)
            throw new InvalidCredentialsException();

        User user = userRepository.findByUserName(userName.trim()).orElseThrow(InvalidCredentialsException::new);

        boolean accountUsable = user.isEnabled() && user.isAccountNonLocked()
                && user.isAccountNonExpired() && user.isCredentialsNonExpired();

        if (!accountUsable || !passwordHasher.matches(password, user.getPassword()))
            throw new InvalidCredentialsException();

        Set<String> roles = user.getPermissions().stream()
                .map(Permission::permissionName)
                .map(PermissionName::value)
                .collect(Collectors.toSet());

        return new AuthenticatedUser(user.getUserName(), roles);
    }
}
