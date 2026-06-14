package com.saldium.saldium.dto.transacao;

import com.saldium.saldium.entidades.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransacaoRequestDTO(
        @NotBlank(message = "Descrição é obrigatório")
        @Schema(description = "Descrição para a transação", example = "compra do mês")
        String descricao,

        @NotNull(message = "Valor é obrigatória")
        @Positive(message = "valor não pode ser negativo")
        @Schema(description = "Valor da transação", example = "530")
        BigDecimal valor,

        @NotNull(message = "Tipo da transação é obrigatório")
        @Schema(description = "Tipo da transação", example = "DESPESA")
        TipoTransacao tipoTransacao,

        @NotNull(message = "Categoria da transação é obrigatória")
        @Schema(description = "ID da categoria da transação", example = "1")
        Long categoria_id
) {
}
