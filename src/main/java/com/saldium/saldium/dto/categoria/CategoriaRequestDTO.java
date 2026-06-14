package com.saldium.saldium.dto.categoria;

import com.saldium.saldium.entidades.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaRequestDTO(
        @NotBlank(message = "Nome da categoria não pode ser vazio")
        @Schema(description = "Nome da categoria", example = "MERCADO")
        String nome,

        @NotNull(message = "Tipo da categoria não pode ser vazio")
        @Schema(description = "Tipo da categoria", example = "DESPESA")
        TipoTransacao tipo
) {
}
