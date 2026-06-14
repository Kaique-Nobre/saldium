package com.saldium.saldium.security.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequestDTO(
        @NotBlank(message = "Senha atual é obrigatório")
        @Schema(description = "Senha atual da conta do usuário")
        String senhaAtual,

        @NotBlank(message = "Nova senha é obrigatório")
        @Schema(description = "Nova senha da conta do usuário")
        String novaSenha,

        @NotBlank(message = "Confirme sua nova senha")
        @Schema(description = "Confirmação da nova senha")
        String confirmarNovaSenha
) {
}
