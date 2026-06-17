package com.saldium.saldium.dto.relatorio;

import java.math.BigDecimal;

public record ResumoMesDTO(
        Integer mes,
        BigDecimal totalRenda,
        BigDecimal totalDespesas
) {
}
