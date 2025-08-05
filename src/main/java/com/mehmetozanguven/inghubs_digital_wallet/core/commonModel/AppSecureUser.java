package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
@SuperBuilder
public class AppSecureUser implements UserDetails {
    private String userId;
    private String username;
    private String password;
    private List<String> userRoles;
    private boolean isEnabled;
    private long registrationDateInMs;
    private long versionId;


    public static AppSecureUser fromUserModel(String userId,
                                              String email,
                                              String password,
                                              List<String> userRoles,
                                              boolean isEnabled) {
        return  AppSecureUser.builder()
                .userId(userId)
                .username(email)
                .password(password)
                .userRoles(userRoles)
                .isEnabled(isEnabled)
                .build();
    }

    public AppSecureUser() {

    }

    public List<SimpleGrantedAuthority> getSimpleRoles() {
        return userRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

}
