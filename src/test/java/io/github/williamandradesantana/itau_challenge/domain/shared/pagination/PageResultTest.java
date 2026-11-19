package io.github.williamandradesantana.itau_challenge.domain.shared.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    @DisplayName("Should map page content preserving pagination metadata")
    void shouldMapPageContent() {

        PageResult<Integer> page = new PageResult<>(
            List.of(1, 2, 3),
            0,
            10,
            25
        );

        PageResult<String> result = page.map(String::valueOf);

        assertEquals(
            List.of("1", "2", "3"),
            result.content()
        );

        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(25, result.totalElements());
        assertEquals(3, result.totalPages());
        assertTrue(result.hasNext());
    }

    @Test
    @DisplayName("Should calculate total pages correctly")
    void shouldCalculateTotalPages() {
        PageResult<Integer> result = new PageResult<>(
            List.of(1, 2, 3),
            0,
            10,
            25
        );

        assertEquals(3, result.totalPages());
    }

    @Test
    @DisplayName("Should return false when current page is the last page")
    void shouldReturnFalseWhenIsLastPage() {
        PageResult<Integer> result = new PageResult<>(
            List.of(1, 2, 3),
            2,
            10,
            25
        );

        assertFalse(result.hasNext());
    }

    @Test
    @DisplayName("Should not modify original page when mapping")
    void shouldPreserveOriginalPageWhenMapping() {
        PageResult<Integer> page = new PageResult<>(
            List.of(1, 2, 3),
            0,
            10,
            25
        );

        PageResult<String> result = page.map(String::valueOf);

        assertEquals(List.of(1, 2, 3), page.content());
        assertEquals(List.of("1", "2", "3"), result.content());
    }
}