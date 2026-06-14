package com.saldium.saldium.security.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroDTO(
        @NotBlank(message = "Nome de usuário é obrigatório")
        @Schema(description = "Nome da conta do usuário")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatório")
        @Size(min = 8, max = 25, message = "Senha deve conter no mínimo 8 caracteres")
        String senha
) {
}
