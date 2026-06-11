package com.saldium.saldium.util.usuario;

import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.Usuario;

import java.time.OffsetDateTime;

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

    public static Usuario criarUsuarioParaTesteDeIntegracao() {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha("password");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    public static Usuario criarAdminParaTesteDeIntegracao() {
        Usuario admin = new Usuario();
        admin.setNome("admin");
        admin.setEmail("admin@email.com");
        admin.setSenha("password");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setCreatedAt(OffsetDateTime.now());
        return admin;
    }
}
