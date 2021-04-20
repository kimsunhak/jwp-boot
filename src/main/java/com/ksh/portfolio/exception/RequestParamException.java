package com.ksh.portfolio.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RequestParamException extends RuntimeException{

    private final String errorCode;

    public RequestParamException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
