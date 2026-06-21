package com.saldium.saldium.dto.relatorio;

import com.saldium.saldium.entidades.TipoTransacao;

import java.math.BigDecimal;

public record RelatorioCategoriaDTO(
        Boolean categoriaDoSistema,
        Long  categoriaId,
        String categoria,
        TipoTransacao tipo,
        BigDecimal total
        ) {
}