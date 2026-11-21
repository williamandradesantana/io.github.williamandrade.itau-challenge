package io.github.williamandradesantana.itau_challenge.support;

import io.github.williamandradesantana.itau_challenge.application.auth.IPasswordHasher;

public class FakePasswordHasher implements IPasswordHasher {
    @Override
    public String hash(String password) {
        return "hashed::" + password;
    }

    @Override
    public boolean matches(String password, String hash) {
        return hash(password).equals(hash);
    }
}
