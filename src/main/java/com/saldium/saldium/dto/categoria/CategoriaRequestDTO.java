package com.saldium.saldium.dto.categoria;

import com.saldium.saldium.entidades.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaRequestDTO(
        @NotBlank(message = "Nome da categoria não pode ser vazio")
        String nome,

        @NotNull(message = "Tipo da categoria não pode ser vazio")
        TipoTransacao tipo
) {
}
