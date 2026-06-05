package com.saldium.saldium.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroDTO(
        @NotBlank(message = "Nome de usuário é obrigatório")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatório")
        @Size(min = 8, max = 25, message = "Senha deve conter no mínimo 8 caracteres")
        String senha
) {
}
