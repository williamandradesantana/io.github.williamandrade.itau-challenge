package io.github.williamandradesantana.itau_challenge.application.auth;

public interface IPasswordHasher {
    String hash(String password);
    boolean matches(String password, String hash);
}
