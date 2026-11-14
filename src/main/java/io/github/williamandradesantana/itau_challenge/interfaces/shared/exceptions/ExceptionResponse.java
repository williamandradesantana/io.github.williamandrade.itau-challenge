package io.github.williamandradesantana.itau_challenge.interfaces.shared.exceptions;

import java.time.Instant;

public record ExceptionResponse(Instant timestamp, String message, String details) {
}
