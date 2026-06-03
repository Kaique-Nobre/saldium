package com.saldium.saldium.exceptions.categoria;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoriaJaExisteException extends BusinessException {
    public CategoriaJaExisteException(String message)
    {
        super(
                "Categoria Já Existe",
                message,
                HttpStatus.CONFLICT
        );
    }
}
