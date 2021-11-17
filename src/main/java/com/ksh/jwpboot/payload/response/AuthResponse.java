package com.ksh.jwpboot.payload.response;


import com.ksh.jwpboot.security.Token;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthResponse {
    private Token accessToken;
    private Token refreshToken;
    private final String tokenType = "Bearer";

    public AuthResponse(Token accessToken, Token refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
