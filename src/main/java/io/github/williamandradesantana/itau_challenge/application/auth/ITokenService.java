package io.github.williamandradesantana.itau_challenge.application.auth;

import java.util.Set;

public interface ITokenService {
    String generateToken(String subject, Set<String> roles);
}
