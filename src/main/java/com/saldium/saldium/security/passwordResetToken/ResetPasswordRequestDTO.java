package com.saldium.saldium.security.passwordResetToken;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDTO(
        @NotBlank
        String novaSenha,

        @NotBlank
        String confirmarNovaSenha
) {
}
