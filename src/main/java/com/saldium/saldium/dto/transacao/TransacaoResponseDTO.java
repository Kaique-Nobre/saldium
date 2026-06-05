package com.saldium.saldium.dto.transacao;

import com.saldium.saldium.entidades.TipoTransacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransacaoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipoTransacao,
        String usuario,
        String categoria,
        OffsetDateTime dataCriacao
) {
}
