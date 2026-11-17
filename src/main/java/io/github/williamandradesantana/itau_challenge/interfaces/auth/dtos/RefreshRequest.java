package io.github.williamandradesantana.itau_challenge.interfaces.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {}
