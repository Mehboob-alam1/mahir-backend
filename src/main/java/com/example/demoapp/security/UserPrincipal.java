package com.example.demoapp.security;

import com.example.demoapp.entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final Role role;
    private final List<GrantedAuthority> authorities;

    public static UserPrincipal create(Long userId, String email, Role role) {
        Role r = role != null ? role : Role.USER;
        List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_" + r.name()));
        return new UserPrincipal(userId, email, r, auths);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
