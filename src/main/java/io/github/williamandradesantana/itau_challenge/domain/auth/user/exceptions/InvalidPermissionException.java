package io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class InvalidPermissionException extends DomainException {
    public InvalidPermissionException(String message) {
        super(message);
    }
}
