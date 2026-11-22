package io.github.williamandradesantana.itau_challenge.interfaces.shared.dtos;

import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    @DisplayName("Test: response should copy the content and every pagination field")
    void test_ResponseShouldCopyTheContentAndEveryPaginationField() {
        PageResult<String> result = new PageResult<>(List.of("a", "b"), 1, 2, 5);

        PageResponse<String> response = PageResponse.response(result);

        assertEquals(List.of("a", "b"), response.content());
        assertEquals(1, response.page());
        assertEquals(2, response.size());
        assertEquals(5L, response.totalElements());
        assertEquals(3, response.totalPages(), () -> "totalPages is a method of PageResult and must be copied into the response");
        assertTrue(response.hasNext(), () -> "hasNext is a method of PageResult and must be copied into the response");
    }

    @Test
    @DisplayName("Test: on the last page, hasNext should be false")
    void test_OnTheLastPage_HasNextShouldBeFalse() {
        PageResponse<String> response = PageResponse.response(new PageResult<>(List.of("e"), 2, 2, 5));

        assertFalse(response.hasNext());
        assertEquals(3, response.totalPages());
    }

    @Test
    @DisplayName("Test: an empty page should have no content and no pages")
    void test_AnEmptyPageShouldHaveNoContentAndNoPages() {
        PageResponse<String> response = PageResponse.response(new PageResult<>(List.of(), 0, 20, 0));

        assertTrue(response.content().isEmpty());
        assertEquals(0L, response.totalElements());
        assertEquals(0, response.totalPages());
        assertFalse(response.hasNext());
    }
}
