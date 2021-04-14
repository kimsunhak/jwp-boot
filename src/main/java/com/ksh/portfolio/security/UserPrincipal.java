package com.ksh.portfolio.security;

import com.ksh.portfolio.domain.member.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserPrincipal implements OAuth2User, UserDetails {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Member member;
    private Collection<? extends  GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(Long id, String name, String email, String password, Member member, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.member = member;
        this.authorities = authorities;
    }

    public static UserPrincipal create(Member member) {
        List<GrantedAuthority> authorityList = Collections.singletonList(new SimpleGrantedAuthority(member.roleName()));
        return new UserPrincipal(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member,
                authorityList);
    }


    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
