package com.app.cloudwebapp.Model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipleDetails implements UserDetails {
    private  User user;
    private String userName;
    private String password;
    private boolean active;
    private List<GrantedAuthority> authorities;

    public UserPrincipleDetails(User user) {
        this.userName = user.getUsername();
        this.password = user.getPassword();
        this.active = user.isActive();

    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;

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
