package io.github.williamandradesantana.itau_challenge.application.auth;

import io.github.williamandradesantana.itau_challenge.application.auth.dtos.AuthenticatedUser;

public interface IAuthenticator {
    AuthenticatedUser authenticate(String userName, String password);
}
