package com.saldium.saldium.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DeletarContaRequestDTO(
        @NotBlank
        String senha
) {
}
