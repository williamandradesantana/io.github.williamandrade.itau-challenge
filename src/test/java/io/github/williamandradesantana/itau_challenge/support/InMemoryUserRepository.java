package io.github.williamandradesantana.itau_challenge.support;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.UserNameAlreadyExistsException;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> users = new LinkedHashMap<>();
    private boolean enforceUniqueUserName = false;

    public InMemoryUserRepository enforceUniqueUserName() {
        this.enforceUniqueUserName = true;
        return this;
    }

    @Override
    public PageResult<User> listAllUsers(PageQuery pageQuery) {
        List<User> sorted = users.values().stream()
                .sorted(Comparator.comparing(User::getUserName))
                .toList();

        int from = Math.min(pageQuery.page() * pageQuery.size(), sorted.size());
        int to = Math.min(from + pageQuery.size(), sorted.size());

        return new PageResult<>(sorted.subList(from, to), pageQuery.page(), pageQuery.size(), sorted.size());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return users.values().stream()
                .filter(user -> user.getUserName().equals(userName))
                .findFirst();
    }

    @Override
    public void save(User user) {
        boolean nameTakenByAnother = users.values().stream()
            .anyMatch(
                existing ->
                    existing.getUserName().equals(user.getUserName()) && !existing.getId().equals(user.getId())
            );
        if (enforceUniqueUserName && nameTakenByAnother)
            throw new UserNameAlreadyExistsException("UserName already exists: " + user.getUserName());

        users.put(user.getId(), user);
    }

    @Override
    public void disable(UUID id) {
        User user = users.get(id);
        if (user != null) user.setEnabled(false);
    }

    @Override
    public void delete(UUID id) {
        users.remove(id);
    }

    public int count() {
        return users.size();
    }

}
