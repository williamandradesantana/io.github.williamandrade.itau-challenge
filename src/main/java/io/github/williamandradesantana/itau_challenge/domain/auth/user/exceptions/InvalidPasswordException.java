package io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class InvalidPasswordException extends DomainException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
