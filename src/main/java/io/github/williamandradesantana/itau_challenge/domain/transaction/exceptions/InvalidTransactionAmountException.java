package io.github.williamandradesantana.itau_challenge.domain.transaction.exceptions;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;

public class InvalidTransactionAmountException extends DomainException {
    public InvalidTransactionAmountException() {
        super("The value cannot be less than zero or null");
    }
}
