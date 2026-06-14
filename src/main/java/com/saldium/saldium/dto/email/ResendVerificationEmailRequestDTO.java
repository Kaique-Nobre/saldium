package com.saldium.saldium.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

public record ResendVerificationEmailRequestDTO(
        @Email
        @Schema(description = "Email para receber link de verificação")
        String email
) {
}
