package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.IPasswordHasher;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.RegisterUserCommand;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.PasswordPolicy;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.UserNameAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);
    private static final String DEFAULT_PERMISSION = "COMMON";

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final IPasswordHasher passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PermissionRepository permissionRepository, IPasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public void execute(RegisterUserCommand command) {
        if (command == null) throw new RequiredObjectIsNullException();

        log.info("Register a user with userName: {}", command.userName());

        PasswordPolicy.validate(command.password());

        Permission defaultPermission = permissionRepository.findByPermissionName(DEFAULT_PERMISSION)
                .orElseThrow(() -> new IllegalStateException("Default permission not seeded: " + DEFAULT_PERMISSION));

        User user = User.create(
            command.userName(),
            command.fullName(),
            passwordHasher.hash(command.password()),
            Set.of(defaultPermission)
        );

        userRepository.findByUserName(user.getUserName()).ifPresent(existing -> {
            throw new UserNameAlreadyExistsException("UserName already exists: " + user.getUserName());
        });

        userRepository.save(user);
    }
}
