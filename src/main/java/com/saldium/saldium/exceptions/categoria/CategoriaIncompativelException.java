package com.saldium.saldium.exceptions.categoria;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoriaIncompativelException extends BusinessException {
    public CategoriaIncompativelException(String message) {

        super(
                "Categoria Incompatível",
                message,
                HttpStatus.BAD_REQUEST);
    }
}
