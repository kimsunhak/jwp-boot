package com.ksh.jwpboot.domain.member;


import com.ksh.jwpboot.domain.base.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = "member_email")
})
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_name", nullable = false)
    private String name;

    @Column(name = "member_email")
    private String email;

    @Column(name = "member_password")
    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "member_role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private String providerId;

    @Column
    private String imageUrl;

    public Member(String name, String email, String password, AuthProvider provider, String providerId, String imageUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.imageUrl = imageUrl;
    }

    public String roleName() {
        return role.name();
    }

    public void updateExistingMember(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public void memberChangePassword(String password) {
        this.password = password;
    }
}

