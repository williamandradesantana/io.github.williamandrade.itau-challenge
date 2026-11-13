package io.github.williamandradesantana.itau_challenge.domain.shared.exceptions;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
