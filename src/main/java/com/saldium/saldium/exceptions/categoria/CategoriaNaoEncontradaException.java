package com.saldium.saldium.exceptions.categoria;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoriaNaoEncontradaException extends BusinessException {
    public CategoriaNaoEncontradaException(String message) {

        super(
                "Categoria Não Encontrada",
                message,
                HttpStatus.NOT_FOUND
        );
    }
}
