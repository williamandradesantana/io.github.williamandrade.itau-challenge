package io.github.williamandradesantana.itau_challenge.application.auth.dtos;

public record RegisterUserCommand(String userName, String fullName, String password) {}
