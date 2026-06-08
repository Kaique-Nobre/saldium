package com.saldium.saldium.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(
        @NotBlank(message = "Token é obrigatório")
        String refreshToken
) {
}
