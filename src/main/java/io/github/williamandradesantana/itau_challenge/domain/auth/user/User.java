package io.github.williamandradesantana.itau_challenge.domain.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidFullNameException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidPasswordException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidPermissionException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidUserNameException;

import java.util.*;

public final class User {

    private final UUID id;
    private String userName;
    private String fullName;
    private String password;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    private final Set<Permission> permissions;

    public User(UUID id, String userName, String fullName, String password, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled, Set<Permission> permissions) {
        this.id = Objects.requireNonNull(id);
        setUserName(userName);
        setFullName(fullName);
        setPassword(password);
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    public static User create(String userName, String fullName, String password, Set<Permission> permissions) {
        return new User(
            UUID.randomUUID(), userName, fullName, password,
            true, true, true, true,
            permissions
        );
    }

    public void updateProfile(String userName, String fullName) {
        setUserName(userName);
        setFullName(fullName);
    }

    public void changePassword(String password) {
        setPassword(password);
    }

    public UUID getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (userName == null || userName.isBlank())
            throw new InvalidUserNameException("User name cannot be null or blank");
        userName = userName.trim();
        if (userName.length() < 3 || userName.length() > 30)
            throw new InvalidUserNameException("The user name must be between 3 and 30 characters");
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank())
            throw new InvalidFullNameException("Full name cannot be null or blank");
        fullName = fullName.trim();
        if (fullName.length() < 3 || fullName.length() > 150)
            throw new InvalidFullNameException("The full name be between 3 and 150 characters");
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank())
            throw new InvalidPasswordException("Password cannot be null or blank");
        password = password.trim();
        if (password.length() < 8 || password.length() > 255)
            throw new InvalidPasswordException("The password must be between 8 and 255 characters");
        this.password = password;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void grantPermission(Permission permission) {
        if (permission == null) throw new InvalidPermissionException("Permission cannot be null");
        this.permissions.add(permission);
    }

    public void revokePermission(Permission permission) {
        if (permission == null) throw new InvalidPermissionException("Permission cannot be null");
        this.permissions.remove(permission);
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return accountNonExpired == user.accountNonExpired && accountNonLocked == user.accountNonLocked && credentialsNonExpired == user.credentialsNonExpired && enabled == user.enabled && Objects.equals(id, user.id) && Objects.equals(userName, user.userName) && Objects.equals(fullName, user.fullName) && Objects.equals(password, user.password) && Objects.equals(permissions, user.permissions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(userName);
        result = 31 * result + Objects.hashCode(fullName);
        result = 31 * result + Objects.hashCode(password);
        result = 31 * result + Boolean.hashCode(accountNonExpired);
        result = 31 * result + Boolean.hashCode(accountNonLocked);
        result = 31 * result + Boolean.hashCode(credentialsNonExpired);
        result = 31 * result + Boolean.hashCode(enabled);
        result = 31 * result + Objects.hashCode(permissions);
        return result;
    }
}
