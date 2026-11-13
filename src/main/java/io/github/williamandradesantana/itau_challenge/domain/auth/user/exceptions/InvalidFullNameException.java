package io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class InvalidFullNameException extends DomainException {
    public InvalidFullNameException(String message) {
        super(message);
    }
}
