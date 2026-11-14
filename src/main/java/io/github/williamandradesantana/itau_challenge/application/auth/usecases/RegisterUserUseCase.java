package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.auth.IPasswordHasher;
import io.github.williamandradesantana.itau_challenge.application.auth.dtos.RegisterUserCommand;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.UserNameAlreadyExistsException;
import io.github.williamandradesantana.itau_challenge.interfaces.shared.exceptions.RequiredObjectIsNullException;

import java.util.Set;

public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final IPasswordHasher passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PermissionRepository permissionRepository, IPasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.passwordHasher = passwordHasher;
    }

    public void execute(RegisterUserCommand command) {
        if (command == null) throw new RequiredObjectIsNullException();

        userRepository.findByUserName(command.userName()).ifPresent((existing) -> {
            throw new UserNameAlreadyExistsException("UserName already exists: " + command.userName());
        });

        Permission defaultPermission = permissionRepository.findByPermissionName("COMMON")
                .orElseThrow(() -> new IllegalStateException("Default permission not seeded: COMMON"));

        String encodedPassword = passwordHasher.hash(command.password());
        User user = User.create(command.userName(), command.fullName(), encodedPassword, Set.of(defaultPermission));

        userRepository.save(user);
    }
}
