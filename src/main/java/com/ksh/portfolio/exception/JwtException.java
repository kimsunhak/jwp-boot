package com.ksh.portfolio.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtException extends RuntimeException {

    private final String errorCode;
    public JwtException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
