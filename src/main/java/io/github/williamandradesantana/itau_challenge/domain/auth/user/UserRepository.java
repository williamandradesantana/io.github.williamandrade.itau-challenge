package io.github.williamandradesantana.itau_challenge.domain.auth.user;

import io.github.williamandradesantana.itau_challenge.interfaces.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.interfaces.shared.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    PageResult<User> listAllUsers(PageQuery pageQuery);
    Optional<User> findById(UUID id);
    Optional<User> findByUserName(String userName);
    void save(User user);
    void disable(UUID id);
    void delete(UUID id);
}
