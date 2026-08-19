package com.merging.chunks.dto;

import com.merging.chunks.enums.ROLES;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class MyUserDetails implements UserDetails, CredentialsContainer {
    @Getter
    private UUID id;
    private String username;
    private String password;
    private ROLES roles;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        assert getRole() != null;
        return List.of(new SimpleGrantedAuthority(getRole().toString()));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    public @Nullable ROLES getRole() {
        return roles;
    }

    @NotNull
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public void eraseCredentials() {
        this.password=null;
    }
}
