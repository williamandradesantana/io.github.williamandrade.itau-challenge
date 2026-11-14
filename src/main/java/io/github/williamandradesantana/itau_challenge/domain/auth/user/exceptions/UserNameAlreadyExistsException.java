package io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class UserNameAlreadyExistsException extends DomainException {
    public UserNameAlreadyExistsException(String message) {
        super(message);
    }
}
