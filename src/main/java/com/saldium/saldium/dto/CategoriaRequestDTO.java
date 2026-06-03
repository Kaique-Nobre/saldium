package com.saldium.saldium.dto;

import com.saldium.saldium.entidades.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaRequestDTO(
        @NotBlank(message = "Nome da categoria não pode ser vazio")
        String nome,

        @NotNull(message = "Tipo da categoria não pode ser vazio")
        TipoCategoria tipo
) {
}
