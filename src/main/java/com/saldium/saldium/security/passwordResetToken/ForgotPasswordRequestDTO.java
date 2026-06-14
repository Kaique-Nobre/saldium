package com.saldium.saldium.security.passwordResetToken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank
        @Email
        @Schema(description = "Email do usuário em que ele receberá o link para alterar sua senha caso tenha a esquecido")
        String email
) {
}
