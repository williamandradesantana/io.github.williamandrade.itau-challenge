package io.github.williamandradesantana.itau_challenge.infrastructure.security;

import io.github.williamandradesantana.itau_challenge.application.auth.IAuthenticator;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthenticatedUser;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpringAuthenticator implements IAuthenticator {

    private final AuthenticationManager authenticationManager;

    public SpringAuthenticator(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthenticatedUser authenticate(String userName, String password) {
        if (userName == null || userName.isBlank() || password == null)
            throw new InvalidCredentialsException();

        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName.trim(), password)
            );

            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return new AuthenticatedUser(authentication.getName(), roles);
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }
    }
}
