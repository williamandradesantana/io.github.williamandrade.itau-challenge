package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.UserResult;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListAllUsersUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListAllUsersUseCase.class);

    private final UserRepository userRepository;

    public ListAllUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResult<UserResult> execute(PageQuery query) {
        if (query == null) throw new RequiredObjectIsNullException();

        log.info("list all users");
        return userRepository.listAllUsers(query).map(UserResult::from);
    }
}
