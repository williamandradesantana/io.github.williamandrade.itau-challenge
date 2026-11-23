package io.github.williamandradesantana.itau_challenge.domain.transaction.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class InvalidTransactionDateTimeException extends DomainException {
    public InvalidTransactionDateTimeException(String message) {
        super(message);
    }
}
