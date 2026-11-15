package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.IAuthenticator;
import io.github.williamandradesantana.itau_challenge.application.auth.ITokenService;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthenticatedUser;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.LoginCommand;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final IAuthenticator authenticator;
    private final ITokenService tokenService;

    public LoginUseCase(IAuthenticator authenticator, ITokenService tokenService) {
        this.authenticator = authenticator;
        this.tokenService = tokenService;
    }

    public String execute(LoginCommand command) {
        if (command == null) throw new RequiredObjectIsNullException();

        log.info("Attempting login for user: {}", command.userName());
        AuthenticatedUser user = authenticator.authenticate(command.userName(), command.password());
        return tokenService.generateToken(user.userName(), user.roles());
    }
}
