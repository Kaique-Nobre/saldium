package com.saldium.saldium.exceptions.transacao;

import com.saldium.saldium.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class TransacaoNaoEncontradaException extends BusinessException {
    public TransacaoNaoEncontradaException(String message) {

        super(
                "Transação Não Encontrada",
                message,
                HttpStatus.NOT_FOUND
        );
    }
}
