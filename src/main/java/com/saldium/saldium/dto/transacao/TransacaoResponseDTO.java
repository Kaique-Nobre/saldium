package com.saldium.saldium.dto.transacao;

import com.saldium.saldium.entidades.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TransacaoResponseDTO(
        @Schema(description = "ID da transação", example = "1")
        Long id,

        @Schema(description = "Descrição da transação", example = "compra do mês")
        String descricao,

        @Schema(description = "Valor da transação", example = "530")
        BigDecimal valor,

        @Schema(description = "TIpo da transação", example = "DESPESA")
        TipoTransacao tipoTransacao,

        @Schema(description = "Email do usuário da transação", example = "usuario@email.com")
        String usuario,

        @Schema(description = "Categoria da transação", example = "MERCADO")
        String categoria,

        @Schema(description = "Data da transação", example = "16/06/2026")
        LocalDate dataTransacao,

        @Schema(description = "Data da criação da transação", example = "2026-06-15T18:42:31.123-03:00")
        OffsetDateTime dataCriacao
) {
}
