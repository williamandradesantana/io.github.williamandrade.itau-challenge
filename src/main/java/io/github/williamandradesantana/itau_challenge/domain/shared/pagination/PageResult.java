package io.github.williamandradesantana.itau_challenge.domain.shared.pagination;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {
    public PageResult {
        if (page < 0 || size <= 0 || totalElements < 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        content = List.copyOf(content);
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return page + 1 < totalPages();
    }

    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResult<>(content.stream().<R>map(mapper).toList(), page, size, totalElements);
    }
}
