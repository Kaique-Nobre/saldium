package com.saldium.saldium.dto.transacao;

import com.saldium.saldium.entidades.TipoTransacao;

import java.time.LocalDate;

public record TransacaoFiltroDTO(
        TipoTransacao tipo,
        Long categoriaId,
        LocalDate dataInicial,
        LocalDate dataFinal
) {
}

