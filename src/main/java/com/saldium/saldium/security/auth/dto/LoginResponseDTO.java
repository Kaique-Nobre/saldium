package com.saldium.saldium.security.auth.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken
) {
}
