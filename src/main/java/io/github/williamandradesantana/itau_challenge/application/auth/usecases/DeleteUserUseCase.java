package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (id == null) throw new RequiredObjectIsNullException();
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        userRepository.delete(id);
    }
}
