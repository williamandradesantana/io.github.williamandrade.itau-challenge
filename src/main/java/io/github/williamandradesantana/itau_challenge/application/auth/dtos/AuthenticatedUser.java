package io.github.williamandradesantana.itau_challenge.application.auth.dtos;

import java.util.Set;

public record AuthenticatedUser(String userName, Set<String> roles) {}
