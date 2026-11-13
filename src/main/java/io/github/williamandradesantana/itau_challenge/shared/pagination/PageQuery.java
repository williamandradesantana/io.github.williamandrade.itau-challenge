package io.github.williamandradesantana.itau_challenge.shared.pagination;

public record PageQuery(int page, int size) {
    public PageQuery {
        if (page < 0 || size <= 0)
            throw new IllegalArgumentException("Invalid pagination parameters");
    }
}
