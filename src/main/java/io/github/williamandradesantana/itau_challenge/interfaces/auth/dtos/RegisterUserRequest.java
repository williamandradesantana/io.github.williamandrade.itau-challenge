package io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.RegisterUserCommand;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank String userName, @NotBlank String fullName, @NotBlank String password
) {
    public static RegisterUserRequest toCommand(RegisterUserCommand command) {
        return new RegisterUserRequest(command.userName(), command.fullName(), command.password());
    }
}
