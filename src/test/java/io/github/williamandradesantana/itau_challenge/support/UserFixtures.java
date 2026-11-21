package io.github.williamandradesantana.itau_challenge.support;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;

import java.util.Set;

public class UserFixtures {

    public static final String RAW_PASSWORD = "Senha@123";

    private UserFixtures(){}

    public static User user(String userName, Permission... permissions) {
        return User.create(
            userName, "Full " + userName, new FakePasswordHasher().hash(RAW_PASSWORD), Set.of(permissions)
        );
    }
}
