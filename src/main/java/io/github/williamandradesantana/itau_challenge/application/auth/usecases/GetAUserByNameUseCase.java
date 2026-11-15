package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.UserResult;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAUserByNameUseCase {

    private final static Logger log = LoggerFactory.getLogger(GetAUserByNameUseCase.class);

    private final UserRepository userRepository;

    public GetAUserByNameUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResult execute(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Get a user with name: {}", userName);
        return UserResult.from(user);
    }
}
