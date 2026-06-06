package com.saldium.saldium.exceptions.auth;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class AcaoNaoAutorizadaException extends BusinessException {
    public AcaoNaoAutorizadaException(String message) {

        super(
                "Ação Não Autorizada",
                message,
                HttpStatus.UNAUTHORIZED);
    }
}
