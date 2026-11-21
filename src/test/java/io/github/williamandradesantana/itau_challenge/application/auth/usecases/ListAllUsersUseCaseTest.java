package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.UserResult;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.InMemoryUserRepository;
import io.github.williamandradesantana.itau_challenge.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListAllUsersUseCaseTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private ListAllUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        useCase = new ListAllUsersUseCase(userRepository);
    }

    private void saveUsers(String... userNames) {
        for (String userName : userNames) {
            userRepository.save(UserFixtures.user(userName, permissionRepository.common));
        }
    }

    private List<String> userNamesOf(PageResult<UserResult> page) {
        return page.content().stream().map(UserResult::userName).toList();
    }

    @Test
    @DisplayName("Test: when there are users, should return them ordered by user name")
    void test_WhenThereAreUsers_ShouldReturnThemOrderedByUserName() {
        saveUsers("carol", "alice", "bob");

        PageResult<UserResult> page = useCase.execute(new PageQuery(0, 10));

        assertEquals(List.of("alice", "bob", "carol"), userNamesOf(page));
        assertEquals(3L, page.totalElements());
    }

    @Test
    @DisplayName("Test: should return only the requested page and the pagination data")
    void test_ShouldReturnOnlyTheRequestedPageAndThePaginationData() {
        saveUsers("user1", "user2", "user3", "user4", "user5");

        PageResult<UserResult> firstPage = useCase.execute(new PageQuery(0, 2));
        PageResult<UserResult> lastPage = useCase.execute(new PageQuery(2, 2));

        assertEquals(List.of("user1", "user2"), userNamesOf(firstPage));
        assertEquals(3, firstPage.totalPages());
        assertTrue(firstPage.hasNext());
        assertEquals(List.of("user5"), userNamesOf(lastPage));
        assertFalse(lastPage.hasNext());
    }

    @Test
    @DisplayName("Test: when the page is beyond the last one, should return an empty content")
    void test_WhenThePageIsBeyondTheLastOne_ShouldReturnAnEmptyContent() {
        saveUsers("alice");

        PageResult<UserResult> page = useCase.execute(new PageQuery(5, 10));

        assertTrue(page.content().isEmpty());
        assertEquals(1L, page.totalElements());
    }

    @Test
    @DisplayName("Test: when there are no users, should return an empty page")
    void test_WhenThereAreNoUsers_ShouldReturnAnEmptyPage() {
        PageResult<UserResult> page = useCase.execute(new PageQuery(0, 10));

        assertTrue(page.content().isEmpty());
        assertEquals(0L, page.totalElements());
        assertEquals(0, page.totalPages());
    }

    @Test
    @DisplayName("Test: disabled users should also be listed")
    void test_DisabledUsersShouldAlsoBeListed() {
        saveUsers("alice", "bob");
        userRepository.findByUserName("bob").orElseThrow().setEnabled(false);

        PageResult<UserResult> page = useCase.execute(new PageQuery(0, 10));

        assertEquals(2, page.content().size());
        assertFalse(page.content().get(1).enabled(), () -> "bob must appear as disabled");
    }

    @Test
    @DisplayName("Test: when the query is null, should throw exception")
    void test_WhenTheQueryIsNull_ShouldThrowException() {
        assertThrows(RequiredObjectIsNullException.class, () -> useCase.execute(null));
    }

}