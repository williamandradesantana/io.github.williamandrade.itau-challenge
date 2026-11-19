package io.github.williamandradesantana.itau_challenge.domain.shared.pagination;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.InvalidPaginationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageQueryTest {

    @Test
    @DisplayName("Test: Should accept a valid query pagination")
    void test_ShouldAcceptAValidQueryPagination() {
        PageQuery query = new PageQuery(0, 10);
        assertEquals(0, query.page(), () -> "The number page not matches");
        assertEquals(10, query.size(), () -> "The number of items does not match");
    }

    @Test
    @DisplayName("Test: Should reject negative page")
    void test_ShouldRejectNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> new PageQuery(-1, 10));
    }

    @Test
    @DisplayName("Test: Should reject zero size")
    void test_ShouldRejectZeroSize() {
        assertThrows(IllegalArgumentException.class, () -> new PageQuery(0, 0));
    }

    @Test
    @DisplayName("Test: Should reject negative size")
    void test_ShouldRejectNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new PageQuery(0, -1));
    }
}