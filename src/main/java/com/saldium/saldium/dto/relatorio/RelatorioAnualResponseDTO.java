package com.saldium.saldium.dto.relatorio;

import java.math.BigDecimal;

public record RelatorioAnualResponseDTO(
        Integer mes,
        BigDecimal totalRenda,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {
}
