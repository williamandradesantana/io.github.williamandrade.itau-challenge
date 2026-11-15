package io.github.williamandradesantana.itau_challenge.interfaces.auth;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.UserResult;
import io.github.williamandradesantana.itau_challenge.application.auth.usecases.*;
import io.github.williamandradesantana.itau_challenge.domain.shared.pagination.PageQuery;
import io.github.williamandradesantana.itau_challenge.interfaces.shared.dtos.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/v1")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final ListAllUsersUseCase listAllUsersUseCase;
    private final GetOneUserUseCase getOneUserUseCase;
    private final GetAUserByNameUseCase getAUserByNameUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(ListAllUsersUseCase listAllUsersUseCase, GetOneUserUseCase getOneUserUseCase, GetAUserByNameUseCase getAUserByNameUseCase, DisableUserUseCase disableUserUseCase, DeleteUserUseCase deleteUserUseCase) {
        this.listAllUsersUseCase = listAllUsersUseCase;
        this.getOneUserUseCase = getOneUserUseCase;
        this.getAUserByNameUseCase = getAUserByNameUseCase;
        this.disableUserUseCase = disableUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<UserResult>> listAllUsers(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PageResponse<UserResult> response = PageResponse.response(listAllUsersUseCase.execute(new PageQuery(page, size)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResult> getAUserByName(Authentication authentication) {
        return ResponseEntity.ok(getAUserByNameUseCase.execute(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResult> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(getOneUserUseCase.execute(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") UUID id) {
        disableUserUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
