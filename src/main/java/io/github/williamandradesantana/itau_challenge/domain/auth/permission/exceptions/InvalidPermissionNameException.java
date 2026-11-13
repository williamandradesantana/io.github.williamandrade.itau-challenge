package io.github.williamandradesantana.itau_challenge.domain.auth.permission.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class InvalidPermissionNameException extends DomainException {
    public InvalidPermissionNameException(String message) {
        super(message);
    }
}
