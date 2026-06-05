package com.saldium.saldium.exceptions.auth;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class BadCredentialsException extends BusinessException {
    public BadCredentialsException(String message) {
        super(
                "Bad Credentials",
                message,
                HttpStatus.UNAUTHORIZED
        );
    }
}
