package com.saldium.saldium.exceptions.auth;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailJaRegistradoException extends BusinessException {
    public EmailJaRegistradoException(String message) {

        super(
                "Email já registrado",
                message,
                HttpStatus.CONFLICT
        );
    }
}
