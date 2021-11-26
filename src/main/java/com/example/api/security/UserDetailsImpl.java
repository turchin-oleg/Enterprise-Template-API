package com.example.api.security;

import com.example.api.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;
    private final Long id;
    private final String username;
    private final String email;
    @JsonIgnore
    private final String password;
    private final long tokenExp;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities,
                           long tokenExp, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.tokenExp = tokenExp;
        this.active = active;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getUserRole().name()));
        return new UserDetailsImpl(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getTokenExp(),
                user.getActive());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}

