package io.github.williamandradesantana.itau_challenge.interfaces.auth;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.LoginCommand;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.RegisterUserCommand;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.UserResult;
import io.github.williamandradesantana.itau_challenge.application.auth.usecases.LoginUseCase;
import io.github.williamandradesantana.itau_challenge.application.auth.usecases.RegisterUserUseCase;
import io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos.LoginRequest;
import io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos.RegisterUserRequest;
import io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping(
        value = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginCommand command = new LoginCommand(request.userName(), request.password());
        String token = loginUseCase.execute(command);
        return ResponseEntity.ok(TokenResponse.bearer(token));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResult> register(@RequestBody @Valid RegisterUserRequest request) {

        RegisterUserCommand command =
                new RegisterUserCommand(request.userName(), request.fullName(), request.password());
        registerUserUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
