package com.saldium.saldium.util.auth;

import com.saldium.saldium.security.auth.dto.CadastroDTO;

public class CadastroCreator {
    public static CadastroDTO criarCadastroDTO() {
        return new CadastroDTO("usuario", "usuario@email.com", "senha123");
    }
}
