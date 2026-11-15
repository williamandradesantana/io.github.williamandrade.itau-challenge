package io.github.williamandradesantana.itau_challenge.interfaces.shared.dtos;

import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;

import java.util.List;

public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean hasNext
)
{
    public static <T> PageResponse<T> response(PageResult<T> result) {
        return new PageResponse<>(
            result.content(), result.page(), result.size(),
            result.totalElements(), result.totalPages(), result.hasNext()
        );
    }
}
