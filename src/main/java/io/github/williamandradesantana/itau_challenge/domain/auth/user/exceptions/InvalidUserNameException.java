package io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class InvalidUserNameException extends DomainException {
    public InvalidUserNameException(String message) {
        super(message);
    }
}
