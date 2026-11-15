package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.UserResult;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;

import java.util.UUID;

public class GetOneUserUseCase {

    private final UserRepository userRepository;

    public GetOneUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResult execute(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResult.from(user);
    }
}
