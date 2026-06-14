package com.saldium.saldium.security.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(
        @NotBlank(message = "Token é obrigatório")
        @Schema(description = "Refresh Token para fazer logout")
        String refreshToken
) {
}
