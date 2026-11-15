package io.github.williamandradesantana.itau_challenge.infrastructure.persistence.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.UserNameAlreadyExistsException;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryImpl implements UserRepository {

    private static final String USER_NAME_CONSTRAINT = "uk_user_name";

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<User> listAllUsers(PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("userName"));
        Page<UserJpaEntity> page = jpaRepository.findAll(pageable);

        List<User> content = page.getContent().stream().map(mapper::toDomain).toList();

        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return jpaRepository.findByUserName(userName).map(mapper::toDomain);
    }

    @Override
    public void save(User user) {
        try {
            jpaRepository.saveAndFlush(mapper.toJpaEntity(user));
        } catch (DataIntegrityViolationException ex) {
            if (isUserNameViolation(ex))
                throw new UserNameAlreadyExistsException("UserName already exists: " + user.getUserName());
            throw ex;
        }
    }

    @Override
    public void disable(UUID id) {
        jpaRepository.disableUser(id);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    private static boolean isUserNameViolation(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && USER_NAME_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}
