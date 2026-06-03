package com.saldium.saldium.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String title;

    protected BusinessException(
            String title,
            String message,
            HttpStatus status) {

        super(message);
        this.title = title;
        this.status = status;
    }

}
