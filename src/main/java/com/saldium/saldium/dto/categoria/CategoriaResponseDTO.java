package com.saldium.saldium.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoriaResponseDTO(
        @Schema(description = "ID da categoria", example = "1")
        Long id,

        @Schema(description = "Nome da categoria", example = "MERCADO")
        String nome,

        @Schema(description = "Tipo da categoria", example = "DESPESA")
        String tipo
) {
}
