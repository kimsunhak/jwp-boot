package com.ksh.jwpboot.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
public class Token {
    private String jwtToken;
    private Date expiryDate;
    private String formattedExpiryDate;

    public Token(String jwtToken, Date expiryDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA);
        this.jwtToken = jwtToken;
        this.expiryDate = expiryDate;
        formattedExpiryDate = simpleDateFormat.format(expiryDate);
    }
}
