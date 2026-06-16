package com.saldium.saldium.util.categorias;

import com.saldium.saldium.dto.categoria.CategoriaRequestDTO;
import com.saldium.saldium.dto.categoria.CategoriaResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.security.user.Usuario;

import static com.saldium.saldium.util.usuario.UsuarioCreator.criarAdmin;
import static com.saldium.saldium.util.usuario.UsuarioCreator.criarUsuario;

public class CategoriasCreator {
    public static CategoriaResponseDTO criarCategoriaResponse() {
        return new CategoriaResponseDTO(1L, "SALÁRIO", "RENDA", true);
    }

    public static CategoriaRequestDTO criarCategoriaRequest() {
        return new CategoriaRequestDTO("salário", TipoTransacao.RENDA);
    }

    public static Categoria criarCategoriaSistema() {
        Usuario admin = criarAdmin();
        Categoria categoria = new Categoria(1L, "SALÁRIO", TipoTransacao.RENDA, true);
        categoria.setUsuario(admin);
        return categoria;
    }

    public static Categoria criarCategoriaDeUsuario() {
        Usuario usuario = criarUsuario();
        Categoria categoria = new Categoria(1L, "SALÁRIO", TipoTransacao.RENDA, false);
        categoria.setUsuario(usuario);
        return categoria;
    }

    public static Categoria criarCategoriaDeUsuarioParaTesteDeIntegracao() {
        Categoria categoria = new Categoria();
        categoria.setNome("JOGOS");
        categoria.setTipo(TipoTransacao.DESPESA);
        categoria.setCategoriaDoSistema(false);
        return categoria;
    }

    public static Categoria criarCategoriaDoSistemaParaTesteDeIntegracao() {
        Categoria categoria = new Categoria();
        categoria.setNome("LAZER");
        categoria.setTipo(TipoTransacao.DESPESA);
        categoria.setCategoriaDoSistema(true);
        return categoria;
    }
}
