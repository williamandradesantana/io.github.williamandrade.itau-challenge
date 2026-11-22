package io.github.williamandradesantana.itau_challenge.domain.shared.pagination;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.InvalidPaginationException;

public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0)
            throw new InvalidPaginationException("The page must be greater than or equal to 0");
        if (size < 1 || size > MAX_SIZE)
            throw new InvalidPaginationException("The size must be between 1 and " + MAX_SIZE);
    }
}
