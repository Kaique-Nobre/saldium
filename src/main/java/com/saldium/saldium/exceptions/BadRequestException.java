package com.saldium.saldium.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(
                "Bad Request",
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}
