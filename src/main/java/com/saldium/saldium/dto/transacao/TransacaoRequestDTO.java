package com.saldium.saldium.dto.transacao;

import com.saldium.saldium.entidades.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransacaoRequestDTO(
        @NotBlank(message = "Descrição é obrigatório")
        String descricao,

        @NotNull(message = "Valor é obrigatória")
        @Positive(message = "valor não pode ser negativo")
        BigDecimal valor,

        @NotNull(message = "Tipo da transação é obrigatório")
        TipoTransacao tipoTransacao,

        @NotNull(message = "Categoria da transação é obrigatória")
        Long categoria_id
) {
}
