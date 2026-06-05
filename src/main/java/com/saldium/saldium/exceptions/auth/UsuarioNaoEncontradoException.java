package com.saldium.saldium.exceptions.auth;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class UsuarioNaoEncontradoException extends BusinessException {
    public UsuarioNaoEncontradoException(String message) {

        super(
                "Usuário não encontrado",
                message,
                HttpStatus.NOT_FOUND
        );
    }
}
