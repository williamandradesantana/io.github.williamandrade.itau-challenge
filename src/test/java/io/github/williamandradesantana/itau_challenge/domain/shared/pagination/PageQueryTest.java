package io.github.williamandradesantana.itau_challenge.domain.shared.pagination;

import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.DomainException;
import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.InvalidPaginationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Test: Should accept the boundaries of the pagination")
    void test_ShouldAcceptTheBoundariesOfThePagination() {
        assertDoesNotThrow(() -> new PageQuery(0, 1));
        assertDoesNotThrow(() -> new PageQuery(0, PageQuery.MAX_SIZE));
    }

    @Test
    @DisplayName("Test: Should reject negative page")
    void test_ShouldRejectNegativePage() {
        InvalidPaginationException exception =
                assertThrows(InvalidPaginationException.class, () -> new PageQuery(-1, 10));

        assertEquals("The page must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    @DisplayName("Test: Should reject zero size")
    void test_ShouldRejectZeroSize() {
        InvalidPaginationException exception =
                assertThrows(InvalidPaginationException.class, () -> new PageQuery(0, 0));

        assertEquals("The size must be between 1 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Test: Should reject negative size")
    void test_ShouldRejectNegativeSize() {
        assertThrows(InvalidPaginationException.class, () -> new PageQuery(0, -1));
    }

    @Test
    @DisplayName("Test: Should reject a size above the maximum")
    void test_ShouldRejectASizeAboveTheMaximum() {
        InvalidPaginationException exception = assertThrows(InvalidPaginationException.class, () -> new PageQuery(0, PageQuery.MAX_SIZE + 1));

        assertEquals("The size must be between 1 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Test: The rejection should be a domain exception, so the api answers 400 and not 500")
    void test_TheRejectionShouldBeADomainException() {
        assertThrows(DomainException.class, () -> new PageQuery(-1, 10));
    }
}
