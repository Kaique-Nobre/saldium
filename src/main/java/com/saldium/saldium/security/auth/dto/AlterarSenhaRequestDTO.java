package com.saldium.saldium.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequestDTO(
        @NotBlank(message = "Senha atual é obrigatório")
        String senhaAtual,

        @NotBlank(message = "Nova senha é obrigatório")
        String novaSenha,

        @NotBlank(message = "Confirme sua nova senha")
        String confirmarNovaSenha
) {
}
