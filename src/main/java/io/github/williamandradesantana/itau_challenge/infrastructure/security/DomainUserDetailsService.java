package io.github.williamandradesantana.itau_challenge.infrastructure.security;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUserName(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String[] authorities = user.getPermissions().stream()
                .map(Permission::permissionName)
                .map(PermissionName::value)
                .toArray(String[]::new);

        return User.withUsername(user.getUserName())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .accountLocked(!user.isAccountNonLocked())
                .accountExpired(!user.isAccountNonExpired())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .build();
    }
}
