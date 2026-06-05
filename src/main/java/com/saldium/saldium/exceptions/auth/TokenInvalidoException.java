package com.saldium.saldium.exceptions.auth;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class TokenInvalidoException extends BusinessException {
    public TokenInvalidoException(String message) {
        super(
                "Token Inválido",
                message,
                HttpStatus.UNAUTHORIZED
        );
    }
}
