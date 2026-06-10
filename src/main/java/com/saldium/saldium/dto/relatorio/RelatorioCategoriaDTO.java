package com.saldium.saldium.dto.relatorio;

import java.math.BigDecimal;

public record RelatorioCategoriaDTO(
        String categoria,
        BigDecimal total
) {
}