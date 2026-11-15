package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.IAuthenticator;
import io.github.williamandradesantana.itau_challenge.application.auth.ITokenService;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthenticatedUser;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.LoginCommand;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;

public class LoginUseCase {

    private final IAuthenticator authenticator;
    private final ITokenService tokenService;

    public LoginUseCase(IAuthenticator authenticator, ITokenService tokenService) {
        this.authenticator = authenticator;
        this.tokenService = tokenService;
    }

    public String execute(LoginCommand command) {
        if (command == null) throw new RequiredObjectIsNullException();
        AuthenticatedUser user = authenticator.authenticate(command.userName(), command.password());
        return tokenService.generateToken(user.userName(), user.roles());
    }
}
