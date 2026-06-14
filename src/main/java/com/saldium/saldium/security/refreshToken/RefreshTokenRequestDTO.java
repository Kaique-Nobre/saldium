package com.saldium.saldium.security.refreshToken;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequestDTO(
        @Schema(description = "Refresh Token do usuário para que ele possa gerar um novo Access Token")
        String refreshToken
) {
}
