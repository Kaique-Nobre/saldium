package com.saldium.saldium.exceptions.categoria;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoriaEmUsoException extends BusinessException {
    public CategoriaEmUsoException(String message) {
        super(
                "Categoria Em Uso",
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}
