package com.ksh.portfolio.payload.response;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberInfoResponse {
    private Long id;
    private String name;
    private String email;
    private String imageUrl;

    public MemberInfoResponse(Long id, String name, String email, String imageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
    }
}
