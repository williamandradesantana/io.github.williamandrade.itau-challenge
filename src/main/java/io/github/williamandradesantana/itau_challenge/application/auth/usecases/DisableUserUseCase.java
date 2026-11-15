package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DisableUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DisableUserUseCase.class);
    private final UserRepository userRepository;

    public DisableUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (id == null) throw new RequiredObjectIsNullException();
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        log.info("Disable user with id: {}", id);
        userRepository.disable(id);
    }
}
