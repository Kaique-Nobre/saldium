package com.saldium.saldium.util.usuario;

import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.Usuario;

public class UsuarioCreator {
    public static Usuario criarAdmin() {
        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setEmail("admin@email.com");
        admin.setRole(Role.ROLE_ADMIN);

        return admin;
    }

    public static Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@email.com");
        usuario.setRole(Role.ROLE_USER);
        return usuario;
    }
}
