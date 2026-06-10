package com.saldium.saldium.dto.relatorio;

import java.math.BigDecimal;

public record RelatorioResposeDTO(
        BigDecimal totalRenda,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {
}
