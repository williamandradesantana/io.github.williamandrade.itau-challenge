package io.github.williamandradesantana.itau_challenge.interfaces.shared.pagination;

import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, int totalElements) {
    public PageResult {
        content = List.copyOf(content);
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return page + 1 < totalPages();
    }
}
