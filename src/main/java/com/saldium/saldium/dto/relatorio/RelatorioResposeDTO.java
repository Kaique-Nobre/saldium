package com.saldium.saldium.dto.relatorio;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record RelatorioResposeDTO(
        @Schema(description = "Quanto obteve de renda", example = "4200")
        BigDecimal totalRenda,

        @Schema(description = "Quanto teve de despesas", example = "3000")
        BigDecimal totalDespesas,

        @Schema(description = "Quanto sobrou (ou faltou caso tenha tido mais despesas do que renda)", example = "1200")
        BigDecimal saldo
) {
}
