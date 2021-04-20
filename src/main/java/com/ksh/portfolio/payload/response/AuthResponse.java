package com.ksh.portfolio.payload.response;


import com.ksh.portfolio.security.Token;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthResponse {
    private Token accessToken;
    private Token refreshToken;
    private String tokenType = "Bearer";

    public AuthResponse(Token accessToken, Token refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
