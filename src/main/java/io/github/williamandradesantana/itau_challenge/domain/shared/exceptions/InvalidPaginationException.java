package io.github.williamandradesantana.itau_challenge.domain.shared.exceptions;

public class InvalidPaginationException extends DomainException {
    public InvalidPaginationException(String message) {
        super(message);
    }
}
